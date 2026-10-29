package com.ibrowse.crm.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ibrowse.crm.config.PaginacaoConfig;
import com.ibrowse.crm.dto.request.InteresseRequest;
import com.ibrowse.crm.dto.response.InteresseResponse;
import com.ibrowse.crm.dto.response.ProdutoPublicoResponse;
import com.ibrowse.crm.entity.Contato;
import com.ibrowse.crm.entity.Empresa;
import com.ibrowse.crm.entity.Interesse;
import com.ibrowse.crm.entity.ItemOportunidade;
import com.ibrowse.crm.entity.Nota;
import com.ibrowse.crm.entity.Oportunidade;
import com.ibrowse.crm.entity.Produto;
import com.ibrowse.crm.entity.Usuario;
import com.ibrowse.crm.enums.StatusInteresse;
import com.ibrowse.crm.enums.StatusOportunidade;
import com.ibrowse.crm.mapper.InteresseMapper;
import com.ibrowse.crm.repository.ContatoRepository;
import com.ibrowse.crm.repository.EmpresaRepository;
import com.ibrowse.crm.repository.InteresseRepository;
import com.ibrowse.crm.repository.ProdutoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InteresseService {

    private static final Set<String> CAMPOS_ORDENACAO = Set.of("id", "dataCriacao", "status", "empresaNome", "nome");
    private static final int TAMANHO_TITULO = 255;

    private final InteresseRepository repository;
    private final InteresseMapper mapper;
    private final ProdutoRepository produtoRepository;
    private final EmpresaRepository empresaRepository;
    private final ContatoRepository contatoRepository;
    private final ReferenciasVenda referencias;
    private final VinculosAcompanhamentoService vinculos;
    private final EntityManager entityManager;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<ProdutoPublicoResponse> produtosDisponiveis() {
        return produtoRepository.findByAtivoTrueOrderByNomeAsc().stream().map(mapper::toProdutoPublico).toList();
    }

    @Transactional
    public void registrar(InteresseRequest request) {
        if (request.paginaPessoal() != null && !request.paginaPessoal().isBlank()) {
            return;
        }
        Set<Long> ids = Set.copyOf(request.produtoIds());
        List<Produto> produtos = produtoRepository.findAllById(ids).stream()
                .filter(produto -> Boolean.TRUE.equals(produto.getAtivo()))
                .toList();
        if (produtos.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Um ou mais produtos escolhidos não estão disponíveis");
        }

        Interesse interesse = mapper.toEntity(request);
        interesse.setNome(request.nome().trim());
        interesse.setEmail(request.email().trim().toLowerCase());
        interesse.setEmpresaNome(request.empresaNome().trim());
        interesse.setCnpj(textoOuNulo(request.cnpj()));
        interesse.setTelefone(textoOuNulo(request.telefone()));
        interesse.setStatus(StatusInteresse.NOVO);
        interesse.setDataConsentimento(LocalDateTime.now(clock));
        interesse.getProdutos().addAll(produtos);
        repository.save(interesse);
    }

    @Transactional(readOnly = true)
    public Page<InteresseResponse> listar(StatusInteresse status, Pageable pageable) {
        PaginacaoConfig.validar(pageable, CAMPOS_ORDENACAO);
        return repository.filtrar(status, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public InteresseResponse buscarPorId(Long id) {
        return mapper.toResponse(repository.findById(id).orElseThrow(() -> naoEncontrado(id)));
    }

    @Transactional
    public InteresseResponse converter(Long id) {
        Interesse interesse = bloquearNovo(id);
        Usuario responsavel = vinculos.usuarioAtivo(vinculos.autor(null));

        Empresa empresa = empresaDoInteresse(interesse, responsavel);
        Contato contato = contatoDoInteresse(interesse, empresa, responsavel);
        List<Produto> produtos = interesse.getProdutos().stream()
                .filter(produto -> Boolean.TRUE.equals(produto.getAtivo()))
                .toList();

        Oportunidade oportunidade = new Oportunidade();
        oportunidade.setTitulo(titulo(interesse, produtos));
        oportunidade.setDescricao(descricao(interesse));
        oportunidade.setEmpresa(empresa);
        oportunidade.setContato(contato);
        oportunidade.setVendedor(responsavel);
        oportunidade.setEstagio(referencias.estagio(null));
        oportunidade.setStatus(StatusOportunidade.ATIVA);
        oportunidade.setValorEstimado(produtos.stream().map(Produto::getPrecoUnitario)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        entityManager.persist(oportunidade);

        for (Produto produto : produtos) {
            ItemOportunidade item = new ItemOportunidade();
            item.setOportunidade(oportunidade);
            item.setProduto(produto);
            item.setQuantidade(1);
            item.setPrecoUnitario(produto.getPrecoUnitario());
            entityManager.persist(item);
        }

        if (interesse.getMensagem() != null && !interesse.getMensagem().isBlank()) {
            Nota nota = new Nota();
            nota.setEmpresa(empresa);
            nota.setContato(contato);
            nota.setOportunidade(oportunidade);
            nota.setUsuario(responsavel);
            nota.setConteudo("Mensagem enviada pela Área do Cliente:\n" + interesse.getMensagem().trim());
            entityManager.persist(nota);
        }

        finalizar(interesse, StatusInteresse.CONVERTIDO, responsavel);
        interesse.setOportunidade(oportunidade);
        entityManager.flush();
        return mapper.toResponse(interesse);
    }

    @Transactional
    public InteresseResponse descartar(Long id) {
        Interesse interesse = bloquearNovo(id);
        finalizar(interesse, StatusInteresse.DESCARTADO, vinculos.usuarioAtivo(vinculos.autor(null)));
        entityManager.flush();
        return mapper.toResponse(interesse);
    }

    private Empresa empresaDoInteresse(Interesse interesse, Usuario responsavel) {
        if (interesse.getCnpj() != null) {
            var existente = empresaRepository.findByCnpj(interesse.getCnpj());
            if (existente.isPresent()) {
                if (!Boolean.TRUE.equals(existente.get().getAtivo())) {
                    throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Já existe a empresa " + existente.get().getNomeRazaoSocial()
                                    + " com este CNPJ, mas ela está inativa. Reative-a antes de converter.");
                }
                return existente.get();
            }
        }
        Empresa empresa = new Empresa();
        empresa.setNomeRazaoSocial(interesse.getEmpresaNome());
        empresa.setCnpj(interesse.getCnpj());
        empresa.setSegmento(interesse.getSegmento());
        empresa.setTamanhoEmpresa(interesse.getTamanhoEmpresa());
        empresa.setSite(interesse.getSite());
        empresa.setAtivo(true);
        empresa.setUsuarioResponsavel(responsavel);
        return empresaRepository.save(empresa);
    }

    private Contato contatoDoInteresse(Interesse interesse, Empresa empresa, Usuario responsavel) {
        if (empresa.getId() != null) {
            var existente = contatoRepository.findFirstByEmpresaIdAndEmailIgnoreCaseAndAtivoTrue(
                    empresa.getId(), interesse.getEmail());
            if (existente.isPresent()) {
                return existente.get();
            }
        }
        Contato contato = new Contato();
        contato.setNome(interesse.getNome());
        contato.setEmail(interesse.getEmail());
        contato.setTelefone(interesse.getTelefone());
        contato.setCargo(interesse.getCargo());
        contato.setEmpresa(empresa);
        contato.setUsuarioResponsavel(responsavel);
        contato.setAtivo(true);
        return contatoRepository.save(contato);
    }

    private String titulo(Interesse interesse, List<Produto> produtos) {
        String nomes = produtos.stream().map(Produto::getNome).collect(Collectors.joining(", "));
        String titulo = interesse.getEmpresaNome() + (nomes.isEmpty() ? "" : " - " + nomes);
        return titulo.length() <= TAMANHO_TITULO ? titulo : titulo.substring(0, TAMANHO_TITULO - 3) + "...";
    }

    private String descricao(Interesse interesse) {
        StringBuilder texto = new StringBuilder("Interesse recebido pela Área do Cliente.");
        if (interesse.getPreferenciaContato() != null) {
            texto.append("\nPreferência de contato: ").append(interesse.getPreferenciaContato());
        }
        if (interesse.getMensagem() != null && !interesse.getMensagem().isBlank()) {
            texto.append("\n\n").append(interesse.getMensagem().trim());
        }
        return texto.toString();
    }

    private Interesse bloquearNovo(Long id) {
        Interesse interesse = entityManager.find(Interesse.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (interesse == null) {
            throw naoEncontrado(id);
        }
        if (interesse.getStatus() != StatusInteresse.NOVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este interesse já foi tratado");
        }
        return interesse;
    }

    private void finalizar(Interesse interesse, StatusInteresse status, Usuario responsavel) {
        interesse.setStatus(status);
        interesse.setUsuarioTratamento(responsavel);
        interesse.setDataTratamento(LocalDateTime.now(clock));
    }

    private ResponseStatusException naoEncontrado(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Interesse " + id + " não encontrado");
    }

    private String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
