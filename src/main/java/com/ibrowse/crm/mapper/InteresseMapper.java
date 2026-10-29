package com.ibrowse.crm.mapper;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.mapstruct.*;
import com.ibrowse.crm.dto.request.InteresseRequest;
import com.ibrowse.crm.dto.response.InteresseResponse;
import com.ibrowse.crm.dto.response.ProdutoPublicoResponse;
import com.ibrowse.crm.entity.Interesse;
import com.ibrowse.crm.entity.Produto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface InteresseMapper {

    @Mapping(target = "oportunidadeId", source = "oportunidade.id")
    @Mapping(target = "usuarioTratamentoNome", source = "usuarioTratamento.nome")
    InteresseResponse toResponse(Interesse entity);

    ProdutoPublicoResponse toProdutoPublico(Produto produto);

    default List<ProdutoPublicoResponse> toProdutosPublicos(Collection<Produto> produtos) {
        return produtos.stream()
                .sorted(Comparator.comparing(Produto::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(this::toProdutoPublico)
                .toList();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "dataConsentimento", ignore = true)
    @Mapping(target = "produtos", ignore = true)
    @Mapping(target = "oportunidade", ignore = true)
    @Mapping(target = "usuarioTratamento", ignore = true)
    @Mapping(target = "dataTratamento", ignore = true)
    @Mapping(target = "discadorStatus", ignore = true)
    @Mapping(target = "discadorTentativas", ignore = true)
    @Mapping(target = "discadorErro", ignore = true)
    @Mapping(target = "discadorEnviadoEm", ignore = true)
    @Mapping(target = "discadorResultadoEm", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    Interesse toEntity(InteresseRequest request);
}
