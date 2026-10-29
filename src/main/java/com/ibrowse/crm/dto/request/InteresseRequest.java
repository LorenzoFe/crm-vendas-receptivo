package com.ibrowse.crm.dto.request;

import java.util.List;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record InteresseRequest(

		@NotBlank(message = "Nome é obrigatório")
		@Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
		String nome,

		@NotBlank(message = "E-mail é obrigatório")
		@Email(message = "E-mail inválido")
		@Size(max = 255, message = "E-mail deve ter no máximo 255 caracteres")
		String email,

		@Pattern(regexp = "\\d{10,11}", message = "Telefone deve ter DDD + número, somente números (10 ou 11 dígitos)")
		String telefone,

		@Size(max = 100, message = "Cargo deve ter no máximo 100 caracteres")
		String cargo,

		@NotBlank(message = "Nome da empresa é obrigatório")
		@Size(max = 255, message = "Nome da empresa deve ter no máximo 255 caracteres")
		String empresaNome,

		@Pattern(regexp = "\\d{14}", message = "CNPJ deve ter 14 dígitos, somente números")
		String cnpj,

		@Size(max = 100, message = "Segmento deve ter no máximo 100 caracteres")
		String segmento,

		@Size(max = 50, message = "Tamanho da empresa deve ter no máximo 50 caracteres")
		String tamanhoEmpresa,

		@Size(max = 255, message = "Site deve ter no máximo 255 caracteres")
		String site,

		@Pattern(regexp = "EMAIL|TELEFONE|WHATSAPP", message = "Preferência de contato inválida")
		String preferenciaContato,

		@Size(max = 2000, message = "Mensagem deve ter no máximo 2000 caracteres")
		String mensagem,

		@NotEmpty(message = "Escolha pelo menos um produto")
		@Size(max = 20, message = "Escolha no máximo 20 produtos")
		List<@NotNull @Positive Long> produtoIds,

		@NotNull(message = "É preciso autorizar o contato")
		@AssertTrue(message = "É preciso autorizar o contato")
		Boolean consentimento,

		String paginaPessoal) {
}
