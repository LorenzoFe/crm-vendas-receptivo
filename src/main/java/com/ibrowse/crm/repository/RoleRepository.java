package com.ibrowse.crm.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ibrowse.crm.entity.Role;

import java.util.Collection;
import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {

    List<Role> findByNomeIn(Collection<String> nomes);

    @EntityGraph(attributePaths = "permissoes")
    List<Role> findAllByOrderByNomeAsc();

}
