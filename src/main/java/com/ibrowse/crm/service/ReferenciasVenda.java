package com.ibrowse.crm.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.ibrowse.crm.entity.Contato;
import com.ibrowse.crm.entity.Empresa;
import com.ibrowse.crm.entity.EstagioVenda;
import com.ibrowse.crm.entity.Usuario;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReferenciasVenda {

    private final EntityManager entityManager;

    public Empresa empresa(Long id) {
        Empresa empresa = encontrar(Empresa.class, id, "Empresa");
        exigirAtivo(empresa.getAtivo(), "Empresa");
        return empresa;
    }

    public Usuario vendedor(Long id) {
        Usuario vendedor = encontrar(Usuario.class, id, "Vendedor");
        exigirAtivo(vendedor.getAtivo(), "Vendedor");
        return vendedor;
    }

    public Contato contato(Long id, Empresa empresa) {
        if (id == null) {
            return null;
        }
        Contato contato = encontrar(Contato.class, id, "Contato");
        exigirAtivo(contato.getAtivo(), "Contato");
        if (!contato.getEmpresa().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O contato deve pertencer à empresa da oportunidade");
        }
        return contato;
    }

    public EstagioVenda estagio(Long id) {
        if (id != null) {
            return encontrar(EstagioVenda.class, id, "Estágio");
        }
        return entityManager.createQuery(
                "select e from EstagioVenda e order by e.ordem, e.id", EstagioVenda.class)
                .setMaxResults(1).getResultStream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Cadastre pelo menos um estágio antes de criar oportunidades"));
    }

    private <T> T encontrar(Class<T> tipo, Long id, String nome) {
        T entidade = entityManager.find(tipo, id);
        if (entidade == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, nome + " " + id + " não encontrado");
        }
        return entidade;
    }

    private void exigirAtivo(Boolean ativo, String nome) {
        if (!Boolean.TRUE.equals(ativo)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, nome + " deve estar ativo");
        }
    }
}
