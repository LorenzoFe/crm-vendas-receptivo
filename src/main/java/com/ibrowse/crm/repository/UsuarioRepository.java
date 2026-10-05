package com.ibrowse.crm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ibrowse.crm.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	@EntityGraph(attributePaths = { "roles", "roles.permissoes" })
	Optional<Usuario> findByEmailIgnoreCase(String email);

}
