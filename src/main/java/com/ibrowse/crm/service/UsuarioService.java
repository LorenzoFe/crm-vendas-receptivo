package com.ibrowse.crm.service;

import com.ibrowse.crm.dto.request.UsuarioRequest;
import com.ibrowse.crm.dto.request.UsuarioUpdateRequest;
import com.ibrowse.crm.dto.response.UsuarioResponse;
import com.ibrowse.crm.entity.Role;
import com.ibrowse.crm.entity.Usuario;
import com.ibrowse.crm.mapper.UsuarioMapper;
import com.ibrowse.crm.repository.RoleRepository;
import com.ibrowse.crm.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(Pageable pageable){
        return  usuarioRepository.findAll(pageable).map(usuarioMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id){
        return usuarioMapper.toResponse(buscaEntidade(id));
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request){
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(email)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um usuário com o e-mail " + email);
        }

        Usuario usuario = usuarioMapper.toEntity(request);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setAtivo(true);
        usuario.setRoles(buscarRoles(request.roles()));

        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioUpdateRequest request){
        Usuario usuario = buscaEntidade(id);

        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um usuário com o e-mail " + email);
        }
        usuarioMapper.atualizar(request, usuario);
        usuario.setEmail(email);
        return usuarioMapper.toResponse(usuarioRepository.saveAndFlush(usuario));
    }

    @Transactional
    public UsuarioResponse alterarRoles(Long id, List<String> nomesRoles){
        Usuario usuario = buscaEntidade(id);
        usuario.setRoles(buscarRoles(nomesRoles));
        return usuarioMapper.toResponse(usuarioRepository.saveAndFlush(usuario));
    }

    @Transactional
    public void desativar(Long id){
        Usuario usuario = buscaEntidade(id);
        usuario.setAtivo(false);
    }

    private Usuario buscaEntidade(Long id){
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário " + id + " não encontrado"));
    }

    private Set<Role> buscarRoles(List<String> nomes){
        Set<String> nomesNormalizado = new HashSet<>();
        for (String nome : nomes){
            nomesNormalizado.add(nome.trim().toUpperCase());
        }

        List<Role> encontradas = roleRepository.findByNomeIn(nomesNormalizado);

        if (encontradas.size() != nomesNormalizado.size()){
            for (Role role : encontradas){
                nomesNormalizado.remove(role.getNome());
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Roles inexistentes: " + nomesNormalizado);
        }
        return new HashSet<>(encontradas);
    }
}
