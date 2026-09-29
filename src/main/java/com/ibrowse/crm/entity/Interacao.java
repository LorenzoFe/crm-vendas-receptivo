package com.ibrowse.crm.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.ibrowse.crm.enums.TipoInteracao;

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

/** Registro de contato com o cliente (ligação, email, reunião...). */
@Entity
@Table(name = "interacoes")
@Getter
@Setter
@NoArgsConstructor
public class Interacao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private TipoInteracao tipo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "empresa_id")
	private Empresa empresa;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contato_id")
	private Contato contato;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "oportunidade_id")
	private Oportunidade oportunidade;

	/** Usuário que registrou a interação. */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String descricao;

	@Column(nullable = false)
	private LocalDateTime dataInteracao;

	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime dataCriacao;

}
