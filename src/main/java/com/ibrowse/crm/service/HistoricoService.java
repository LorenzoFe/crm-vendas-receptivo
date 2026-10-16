package com.ibrowse.crm.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.ibrowse.crm.dto.response.HistoricoResponse;
import com.ibrowse.crm.repository.OportunidadeRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistoricoService {

    private static final String UNIAO = """
            SELECT id, 'INTERACAO' AS origem, tipo, descricao AS conteudo,
                data_interacao AS data_evento, usuario_id
            FROM interacoes WHERE oportunidade_id = :oportunidadeId
            UNION ALL
            SELECT id, 'NOTA' AS origem, CAST(NULL AS varchar(50)) AS tipo, conteudo,
                data_criacao AS data_evento, usuario_id
            FROM notas WHERE oportunidade_id = :oportunidadeId
            """;
    private static final String CONSULTA = "SELECT * FROM (" + UNIAO + """
            ) eventos ORDER BY data_evento DESC, origem ASC, id DESC
            LIMIT :limite OFFSET :deslocamento
            """;
    private static final String CONTAGEM = "SELECT count(*) FROM (" + UNIAO + ") eventos";

    private final OportunidadeRepository oportunidades;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public Page<HistoricoResponse> listar(Long oportunidadeId, Pageable pageable) {
        Pageable validada = VinculosAcompanhamentoService.pagina(pageable);
        Pageable pagina = PageRequest.of(validada.getPageNumber(), validada.getPageSize());
        if (!oportunidades.existsById(oportunidadeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Oportunidade não encontrada");
        }
        long total = ((Number) entityManager.createNativeQuery(CONTAGEM)
                .setParameter("oportunidadeId", oportunidadeId).getSingleResult()).longValue();
        List<?> registros = entityManager.createNativeQuery(CONSULTA)
                .setParameter("oportunidadeId", oportunidadeId)
                .setParameter("limite", pagina.getPageSize())
                .setParameter("deslocamento", pagina.getOffset())
                .getResultList();
        List<HistoricoResponse> conteudo = registros.stream()
                .map(registro -> converter((Object[]) registro)).toList();
        return new PageImpl<>(conteudo, pagina, total);
    }

    private HistoricoResponse converter(Object[] registro) {
        return new HistoricoResponse(((Number) registro[0]).longValue(),
                (String) registro[1], (String) registro[2], (String) registro[3],
                data(registro[4]), ((Number) registro[5]).longValue());
    }

    private LocalDateTime data(Object valor) {
        if (valor instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return (LocalDateTime) valor;
    }
}
