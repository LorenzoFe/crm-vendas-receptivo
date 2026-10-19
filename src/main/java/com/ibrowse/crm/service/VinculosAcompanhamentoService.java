package com.ibrowse.crm.service;

import java.util.Objects;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ibrowse.crm.security.UsuarioAutenticado;
import org.springframework.web.server.ResponseStatusException;
import com.ibrowse.crm.entity.*;
import com.ibrowse.crm.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VinculosAcompanhamentoService {

    private final EmpresaRepository empresas;
    private final ContatoRepository contatos;
    private final UsuarioRepository usuarios;
    private final EntityManager entityManager;

    public Vinculos resolver(Long empresaId, Long contatoId, Long oportunidadeId, boolean obrigatorio) {
        if (obrigatorio && empresaId == null && contatoId == null && oportunidadeId == null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Informe ao menos uma empresa, contato ou oportunidade");
        }
        Oportunidade oportunidade = oportunidadeId == null ? null : buscarOportunidade(oportunidadeId);
        Empresa empresa = empresaId == null ? null : empresas.findById(empresaId)
                .orElseThrow(() -> ausente("Empresa"));
        Contato contato = contatoId == null ? null : contatos.findById(contatoId)
                .orElseThrow(() -> ausente("Contato"));
        conferirEmpresas(empresa, contato, oportunidade);
        return new Vinculos(empresa, contato, oportunidade);
    }

    public Long autor(Long usuarioIdInformado) {
        Long logado = usuarioLogado().id();
        if (usuarioIdInformado != null && !Objects.equals(logado, usuarioIdInformado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Somente o próprio usuário pode registrar ou editar sua autoria");
        }
        return logado;
    }

    public void validarAutoria(Long usuarioId) {
        if (!Objects.equals(usuarioLogado().id(), usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Somente o próprio usuário pode registrar ou editar sua autoria");
        }
    }

    private UsuarioAutenticado usuarioLogado() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autenticação necessária");
        }
        return usuario;
    }

    public Usuario usuarioAtivo(Long id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário é obrigatório");
        }
        Usuario usuario = usuarios.findById(id).orElseThrow(() -> ausente("Usuário"));
        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Usuário está inativo");
        }
        return usuario;
    }

    public static Pageable pagina(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged() || pageable.getPageSize() > 100
                || pageable.getOffset() > Integer.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe página válida e tamanho entre 1 e 100");
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "id"));
    }

    private Oportunidade buscarOportunidade(Long id) {
        Oportunidade oportunidade = entityManager.find(Oportunidade.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (oportunidade == null) {
            throw ausente("Oportunidade");
        }
        return oportunidade;
    }

    private void conferirEmpresas(Empresa empresa, Contato contato, Oportunidade oportunidade) {
        Long empresaEsperada = empresa == null ? null : empresa.getId();
        if (contato != null && empresaEsperada == null) {
            empresaEsperada = contato.getEmpresa().getId();
        }
        if (contato != null && !Objects.equals(empresaEsperada, contato.getEmpresa().getId())) {
            throw conflitoVinculos();
        }
        if (oportunidade != null && empresaEsperada != null
                && !Objects.equals(empresaEsperada, oportunidade.getEmpresa().getId())) {
            throw conflitoVinculos();
        }
    }

    private ResponseStatusException conflitoVinculos() {
        return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                "Empresa, contato e oportunidade devem pertencer à mesma empresa");
    }

    private ResponseStatusException ausente(String recurso) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, recurso + " não encontrado");
    }

    public record Vinculos(Empresa empresa, Contato contato, Oportunidade oportunidade) {
    }
}
