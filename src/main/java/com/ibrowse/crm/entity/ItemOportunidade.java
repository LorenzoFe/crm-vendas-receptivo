package com.ibrowse.crm.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

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

/** Produto incluído em uma oportunidade, com quantidade e preço negociado. */
@Entity
@Table(name = "itens_oportunidade")
@Getter
@Setter
@NoArgsConstructor
public class ItemOportunidade {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "oportunidade_id", nullable = false)
	private Oportunidade oportunidade;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "produto_id", nullable = false)
	private Produto produto;

	@Column(nullable = false)
	private Integer quantidade;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal precoUnitario;

	@Generated(event = { EventType.INSERT, EventType.UPDATE })
	@Column(precision = 15, scale = 2, insertable = false, updatable = false)
	@Setter(lombok.AccessLevel.NONE)
	private BigDecimal subtotal;

	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime dataCriacao;

}
