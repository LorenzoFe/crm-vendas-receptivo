package com.ibrowse.crm.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Etapa do funil de vendas (colunas do kanban). */
@Entity
@Table(name = "estagios_vendas")
@Getter
@Setter
@NoArgsConstructor
public class EstagioVenda {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String nome;

	@Column(nullable = false)
	private Integer ordem;

	private Integer probabilidadeConversao;

	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime dataCriacao;

}
