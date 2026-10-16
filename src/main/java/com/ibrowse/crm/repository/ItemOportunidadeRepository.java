package com.ibrowse.crm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ibrowse.crm.entity.ItemOportunidade;

public interface ItemOportunidadeRepository extends JpaRepository<ItemOportunidade, Long> {

    @EntityGraph(attributePaths = "produto")
    List<ItemOportunidade> findByOportunidadeIdOrderByIdAsc(Long oportunidadeId);

    Optional<ItemOportunidade> findByIdAndOportunidadeId(Long id, Long oportunidadeId);
}
