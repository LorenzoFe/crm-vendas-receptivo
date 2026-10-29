package com.ibrowse.crm.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ibrowse.crm.entity.Interesse;
import com.ibrowse.crm.enums.StatusDiscador;
import com.ibrowse.crm.enums.StatusInteresse;

public interface InteresseRepository extends JpaRepository<Interesse, Long> {

	@EntityGraph(attributePaths = { "oportunidade", "usuarioTratamento" })
	@Query("""
			select i from Interesse i
			where (:status is null or i.status = :status)
			""")
	Page<Interesse> filtrar(@Param("status") StatusInteresse status, Pageable pageable);

	List<Interesse> findTop50ByStatusNotAndDiscadorStatusInAndDiscadorTentativasLessThanOrderByIdAsc(
			StatusInteresse status, Collection<StatusDiscador> discadorStatus, Integer tentativas);

}
