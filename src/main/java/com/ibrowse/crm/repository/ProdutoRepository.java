package com.ibrowse.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ibrowse.crm.entity.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

}
