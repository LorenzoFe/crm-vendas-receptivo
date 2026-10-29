package com.ibrowse.crm.entity;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;

import com.ibrowse.crm.enums.StatusDiscador;
import com.ibrowse.crm.enums.StatusInteresse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Pedido de contato enviado pela Área do Cliente (página pública), antes de virar empresa/contato/oportunidade. */
@Entity
@Table(name = "interesses")
@Getter
@Setter
@NoArgsConstructor
public class Interesse {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false)
	private String email;

	@Column(length = 20)
	private String telefone;

	@Column(length = 100)
	private String cargo;

	@Column(nullable = false)
	private String empresaNome;

	@Column(length = 14)
	private String cnpj;

	@Column(length = 100)
	private String segmento;

	@Column(length = 50)
	private String tamanhoEmpresa;

	private String site;

	@Column(length = 20)
	private String preferenciaContato;

	@Column(columnDefinition = "TEXT")
	private String mensagem;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StatusInteresse status;

	@Column(nullable = false)
	private LocalDateTime dataConsentimento;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "interesse_produtos",
			joinColumns = @JoinColumn(name = "interesse_id"),
			inverseJoinColumns = @JoinColumn(name = "produto_id"))
	@BatchSize(size = 50)
	private Set<Produto> produtos = new LinkedHashSet<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "oportunidade_id")
	private Oportunidade oportunidade;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_tratamento_id")
	private Usuario usuarioTratamento;

	private LocalDateTime dataTratamento;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private StatusDiscador discadorStatus;

	@Column(nullable = false)
	private Integer discadorTentativas = 0;

	@Column(length = 500)
	private String discadorErro;

	private LocalDateTime discadorEnviadoEm;

	private LocalDateTime discadorResultadoEm;

	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime dataCriacao;

}
