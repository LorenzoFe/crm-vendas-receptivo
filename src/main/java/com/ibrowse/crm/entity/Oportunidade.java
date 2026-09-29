package com.ibrowse.crm.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.ibrowse.crm.enums.StatusOportunidade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Negociação em andamento com uma empresa cliente. É o centro do CRM. */
@Entity
@Table(name = "oportunidades")
@Getter
@Setter
@NoArgsConstructor
public class Oportunidade {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String titulo;

	@Column(columnDefinition = "TEXT")
	private String descricao;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "empresa_id", nullable = false)
	private Empresa empresa;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contato_id")
	private Contato contato;

	@Column(precision = 15, scale = 2)
	private BigDecimal valorEstimado;

	@Column(precision = 15, scale = 2)
	private BigDecimal valorReal;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "estagio_id", nullable = false)
	private EstagioVenda estagio;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_vendedor_id", nullable = false)
	private Usuario vendedor;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private StatusOportunidade status;

	private String motivoPerda;

	private LocalDate dataFechamentoEsperada;

	private LocalDate dataFechamentoReal;

	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime dataCriacao;

	@UpdateTimestamp
	private LocalDateTime dataAtualizacao;

}
