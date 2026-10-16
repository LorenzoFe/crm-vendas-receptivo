package com.ibrowse.crm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ibrowse.crm.entity.EstagioVenda;

public interface EstagioVendaRepository extends JpaRepository<EstagioVenda, Long> {

    List<EstagioVenda> findAllByOrderByOrdemAscIdAsc();

    boolean existsByNome(String nome);

    boolean existsByNomeAndIdNot(String nome, Long id);

    @Query("select (count(o) > 0) from Oportunidade o where o.estagio.id = :estagioId")
    boolean existeOportunidadeVinculada(@Param("estagioId") Long estagioId);
}
