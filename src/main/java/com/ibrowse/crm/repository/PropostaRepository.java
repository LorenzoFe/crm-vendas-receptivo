package com.ibrowse.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

import com.ibrowse.crm.entity.Proposta;
import com.ibrowse.crm.enums.StatusProposta;

public interface PropostaRepository extends JpaRepository<Proposta, Long> {

    @Query("select p.oportunidade.id from Proposta p where p.id = :id")
    Optional<Long> buscarOportunidadeId(@Param("id") Long id);

    @Query("select p from Proposta p where (:status is null or p.status = :status) "
            + "and (:oportunidadeId is null or p.oportunidade.id = :oportunidadeId)")
    Page<Proposta> filtrar(@Param("status") StatusProposta status,
            @Param("oportunidadeId") Long oportunidadeId, Pageable pageable);

    @Query(value = "select max(numero) from propostas where numero ~ :padrao", nativeQuery = true)
    Optional<String> buscarMaiorNumeroValido(@Param("padrao") String padrao);
}
