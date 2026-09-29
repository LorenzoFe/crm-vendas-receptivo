package com.ibrowse.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ibrowse.crm.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

}
