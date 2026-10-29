package com.ibrowse.crm.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ibrowse.crm.entity.Contato;

public interface ContatoRepository extends JpaRepository<Contato, Long> {

	@EntityGraph(attributePaths = { "empresa", "usuarioResponsavel" })
	@Query("""
			select c from Contato c
			where c.ativo = :ativo
			  and lower(c.nome) like lower(concat('%', :nome, '%'))
			  and (:empresaId is null or c.empresa.id = :empresaId)
			""")
	Page<Contato> filtrar(@Param("nome") String nome,
			@Param("empresaId") Long empresaId,
			@Param("ativo") Boolean ativo,
			Pageable pageable);

	Optional<Contato> findFirstByEmpresaIdAndEmailIgnoreCaseAndAtivoTrue(Long empresaId, String email);

}
