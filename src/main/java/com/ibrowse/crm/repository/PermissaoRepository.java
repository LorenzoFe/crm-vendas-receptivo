package com.ibrowse.crm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ibrowse.crm.entity.Permissao;

public interface PermissaoRepository extends JpaRepository<Permissao, Long> {

	List<Permissao> findAllByOrderByModuloAscNomeAsc();

}
