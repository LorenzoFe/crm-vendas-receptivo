package com.ibrowse.crm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ibrowse.crm.entity.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

	Page<Produto> findByNomeContainingIgnoreCaseAndAtivo(String nome, Boolean ativo, Pageable pageable);

}
