package com.ibrowse.crm.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ibrowse.crm.entity.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

	@EntityGraph(attributePaths = "usuarioResponsavel")
	@Query("""
			select e from Empresa e
			where e.ativo = :ativo
			  and lower(e.nomeRazaoSocial) like lower(concat('%', :nome, '%'))
			  and (:segmento = '' or lower(e.segmento) = lower(:segmento))
			  and (:responsavelId is null or e.usuarioResponsavel.id = :responsavelId)
			""")
	Page<Empresa> filtrar(@Param("nome") String nome,
			@Param("segmento") String segmento,
			@Param("responsavelId") Long responsavelId,
			@Param("ativo") Boolean ativo,
			Pageable pageable);

	boolean existsByCnpj(String cnpj);

	Optional<Empresa> findByCnpj(String cnpj);

	boolean existsByCnpjAndIdNot(String cnpj, Long id);

}
