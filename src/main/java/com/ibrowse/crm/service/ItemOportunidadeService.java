package com.ibrowse.crm.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ibrowse.crm.dto.request.AtualizarItemRequest;
import com.ibrowse.crm.dto.request.ItemOportunidadeRequest;
import com.ibrowse.crm.dto.response.ItemOportunidadeResponse;
import com.ibrowse.crm.entity.ItemOportunidade;
import com.ibrowse.crm.entity.Oportunidade;
import com.ibrowse.crm.entity.Produto;
import com.ibrowse.crm.enums.StatusOportunidade;
import com.ibrowse.crm.mapper.ItemOportunidadeMapper;
import com.ibrowse.crm.repository.ItemOportunidadeRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemOportunidadeService {

    private final EntityManager entityManager;
    private final ItemOportunidadeRepository repository;
    private final ItemOportunidadeMapper mapper;

    @Transactional(readOnly = true)
    public List<ItemOportunidadeResponse> listar(Long oportunidadeId) {
        encontrarOportunidade(oportunidadeId, false);
        return repository.findByOportunidadeIdOrderByIdAsc(oportunidadeId)
                .stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public ItemOportunidadeResponse adicionar(Long oportunidadeId, ItemOportunidadeRequest request) {
        Oportunidade oportunidade = encontrarOportunidade(oportunidadeId, true);
        Produto produto = entityManager.find(Produto.class, request.produtoId());
        if (produto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado");
        }
        exigirProdutoAtivo(produto);
        ItemOportunidade item = new ItemOportunidade();
        item.setOportunidade(oportunidade);
        item.setProduto(produto);
        item.setQuantidade(request.quantidade());
        item.setPrecoUnitario(request.precoUnitario() == null
                ? produto.getPrecoUnitario() : request.precoUnitario());
        entityManager.persist(item);
        return sincronizar(item);
    }

    @Transactional
    public ItemOportunidadeResponse atualizar(Long oportunidadeId, Long itemId, AtualizarItemRequest request) {
        encontrarOportunidade(oportunidadeId, true);
        ItemOportunidade item = encontrarItem(oportunidadeId, itemId);
        exigirProdutoAtivo(item.getProduto());
        item.setQuantidade(request.quantidade());
        if (request.precoUnitario() != null) {
            item.setPrecoUnitario(request.precoUnitario());
        }
        return sincronizar(item);
    }

    @Transactional
    public void remover(Long oportunidadeId, Long itemId) {
        encontrarOportunidade(oportunidadeId, true);
        entityManager.remove(encontrarItem(oportunidadeId, itemId));
        entityManager.flush();
    }

    private ItemOportunidadeResponse sincronizar(ItemOportunidade item) {
        entityManager.flush();
        entityManager.refresh(item);
        return mapper.toResponse(item);
    }

    private ItemOportunidade encontrarItem(Long oportunidadeId, Long itemId) {
        return repository.findByIdAndOportunidadeId(itemId, oportunidadeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Item não encontrado nesta oportunidade"));
    }

    private Oportunidade encontrarOportunidade(Long id, boolean escrita) {
        Oportunidade oportunidade = entityManager.find(Oportunidade.class, id,
                escrita ? LockModeType.PESSIMISTIC_WRITE : LockModeType.NONE);
        if (oportunidade == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Oportunidade não encontrada");
        }
        if (escrita && oportunidade.getStatus() != StatusOportunidade.ATIVA) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Itens só podem ser alterados em uma oportunidade ATIVA");
        }
        return oportunidade;
    }

    private void exigirProdutoAtivo(Produto produto) {
        if (!Boolean.TRUE.equals(produto.getAtivo())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Produto deve estar ativo");
        }
    }
}
