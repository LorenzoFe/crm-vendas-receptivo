package com.ibrowse.crm.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/** Empresa cliente (alvo de venda). */
@Entity
@Table(name = "empresas")
@Getter
@Setter
@NoArgsConstructor
public class Empresa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nomeRazaoSocial;

	@Column(unique = true, length = 14)
	private String cnpj;

	@Column(length = 100)
	private String segmento;

	@Column(length = 50)
	private String tamanhoEmpresa;

	@Column(length = 20)
	private String telefone;

	private String email;

	private String site;

	private Boolean ativo = true;

	/** Vendedor interno responsável pela conta. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_responsavel_id")
	private Usuario usuarioResponsavel;

	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime dataCriacao;

	@UpdateTimestamp
	private LocalDateTime dataAtualizacao;

}
