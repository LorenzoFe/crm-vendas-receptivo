package com.ibrowse.crm.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProdutoRequest(

		@NotBlank(message = "Nome é obrigatório")
		@Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
		String nome,

		String descricao,

		@NotNull(message = "Preço unitário é obrigatório")
		@DecimalMin(value = "0.00", message = "Preço unitário não pode ser negativo")
		@Digits(integer = 10, fraction = 2, message = "Preço unitário deve ter no máximo 10 dígitos inteiros e 2 decimais")
		BigDecimal precoUnitario,

		@Size(max = 100, message = "Categoria deve ter no máximo 100 caracteres")
		String categoria) {
}
