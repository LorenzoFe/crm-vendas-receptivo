package com.ibrowse.crm.mapper;

import com.ibrowse.crm.dto.request.UsuarioRequest;
import com.ibrowse.crm.dto.request.UsuarioUpdateRequest;
import com.ibrowse.crm.dto.response.UsuarioResponse;
import com.ibrowse.crm.entity.Role;
import com.ibrowse.crm.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UsuarioMapper {

    UsuarioResponse toResponse(Usuario usuario);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senha", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Usuario toEntity(UsuarioRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senha", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    void atualizar(UsuarioUpdateRequest request, @MappingTarget Usuario usuario);

    default List<String> nomesDasRoles(Set<Role> roles){
        return roles.stream()
                .map(Role::getNome)
                .sorted()
                .toList();
    }
}
