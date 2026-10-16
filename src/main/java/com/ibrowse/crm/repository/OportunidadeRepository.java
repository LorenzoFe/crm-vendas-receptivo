package com.ibrowse.crm.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ibrowse.crm.entity.Oportunidade;
import com.ibrowse.crm.enums.StatusOportunidade;

public interface OportunidadeRepository extends JpaRepository<Oportunidade, Long> {

    @EntityGraph(attributePaths = {"empresa", "contato", "vendedor", "estagio"})
    @Query("""
            select o from Oportunidade o
            where (:status is null or o.status = :status)
              and (:vendedorId is null or o.vendedor.id = :vendedorId)
              and (:estagioId is null or o.estagio.id = :estagioId)
              and (:empresaId is null or o.empresa.id = :empresaId)
            """)
    Page<Oportunidade> filtrar(@Param("status") StatusOportunidade status,
            @Param("vendedorId") Long vendedorId, @Param("estagioId") Long estagioId,
            @Param("empresaId") Long empresaId, Pageable pageable);

    @EntityGraph(attributePaths = {"empresa", "contato", "vendedor", "estagio"})
    @Query("""
            select o from Oportunidade o
            where o.status = :status and (:vendedorId is null or o.vendedor.id = :vendedorId)
            order by o.id
            """)
    List<Oportunidade> buscarParaKanban(@Param("status") StatusOportunidade status,
            @Param("vendedorId") Long vendedorId);

    @Query("""
            select o.estagio.id as estagioId, count(o) as quantidade,
                   coalesce(sum(o.valorEstimado), 0) as valorEstimado
            from Oportunidade o where o.status = :status group by o.estagio.id
            """)
    List<PipelineResumo> resumirPipeline(@Param("status") StatusOportunidade status);

    @Query("""
            select (count(o) > 0) from Oportunidade o where o.id = :id and (
                exists (select i.id from ItemOportunidade i where i.oportunidade.id = o.id)
                or exists (select p.id from Proposta p where p.oportunidade.id = o.id)
                or exists (select t.id from Tarefa t where t.oportunidade.id = o.id)
                or exists (select i.id from Interacao i where i.oportunidade.id = o.id)
                or exists (select n.id from Nota n where n.oportunidade.id = o.id))
            """)
    boolean possuiDependencias(@Param("id") Long id);
}
