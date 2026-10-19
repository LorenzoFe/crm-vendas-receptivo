package com.ibrowse.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

import com.ibrowse.crm.entity.Tarefa;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    @Query(value = """
            SELECT t.* FROM tarefas t
            WHERE t.status <> 'CONCLUIDA' AND t.data_vencimento <= :limite
            ORDER BY t.data_vencimento ASC,
                CASE t.prioridade WHEN 'ALTA' THEN 0 WHEN 'MEDIA' THEN 1 ELSE 2 END ASC,
                t.id ASC
            """, countQuery = """
            SELECT count(*) FROM tarefas t
            WHERE t.status <> 'CONCLUIDA' AND t.data_vencimento <= :limite
            """, nativeQuery = true)
    Page<Tarefa> buscarUrgentes(@Param("limite") LocalDate limite, Pageable pageable);

}
