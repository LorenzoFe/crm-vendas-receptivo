package com.ibrowse.crm.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ibrowse.crm.entity.Role;
import com.ibrowse.crm.entity.Usuario;
import com.ibrowse.crm.repository.RoleRepository;
import com.ibrowse.crm.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInicial implements ApplicationRunner {

	private final UsuarioRepository usuarioRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.admin.email}")
	private String email;

	@Value("${app.admin.senha}")
	private String senha;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (usuarioRepository.existsByEmailIgnoreCase(email)) {
			return;
		}
		List<Role> roles = roleRepository.findByNomeIn(List.of("ADMIN"));
		if (roles.isEmpty()) {
			log.warn("Role ADMIN não encontrada; admin inicial não foi criado");
			return;
		}
		Usuario admin = new Usuario();
		admin.setNome("Administrador");
		admin.setEmail(email.trim().toLowerCase());
		admin.setSenha(passwordEncoder.encode(senha));
		admin.setAtivo(true);
		admin.getRoles().addAll(roles);
		usuarioRepository.save(admin);
		log.info("Admin inicial criado: {}", admin.getEmail());
	}

}
