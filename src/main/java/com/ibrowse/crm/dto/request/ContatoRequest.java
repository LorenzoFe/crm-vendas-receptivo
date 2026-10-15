package com.ibrowse.crm.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ContatoRequest(

		@NotBlank(message = "Nome é obrigatório")
		@Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
		String nome,

		@Email(message = "E-mail inválido")
		@Size(max = 255, message = "E-mail deve ter no máximo 255 caracteres")
		String email,

		@Pattern(regexp = "\\d{10,11}", message = "Telefone deve ter DDD + número, somente números (10 ou 11 dígitos)")
		String telefone,

		@Size(max = 100, message = "Cargo deve ter no máximo 100 caracteres")
		String cargo,

		@NotNull(message = "Empresa é obrigatória")
		Long empresaId,

		Long usuarioResponsavelId) {
}
