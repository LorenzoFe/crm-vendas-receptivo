package com.ibrowse.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ibrowse.crm.entity.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

}
