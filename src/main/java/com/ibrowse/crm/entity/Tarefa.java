package com.ibrowse.crm.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.ibrowse.crm.enums.PrioridadeTarefa;
import com.ibrowse.crm.enums.StatusTarefa;

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

/** Atividade a ser feita por um usuário (follow-up, enviar proposta...). */
@Entity
@Table(name = "tarefas")
@Getter
@Setter
@NoArgsConstructor
public class Tarefa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String titulo;

	@Column(columnDefinition = "TEXT")
	private String descricao;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "oportunidade_id")
	private Oportunidade oportunidade;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contato_id")
	private Contato contato;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "empresa_id")
	private Empresa empresa;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_responsavel_id", nullable = false)
	private Usuario responsavel;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private StatusTarefa status;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private PrioridadeTarefa prioridade;

	private LocalDate dataVencimento;

	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime dataCriacao;

	private LocalDateTime dataConclusao;

	@UpdateTimestamp
	private LocalDateTime dataAtualizacao;

}
