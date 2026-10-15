package com.ibrowse.crm.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmpresaRequest(

		@NotBlank(message = "Nome/razão social é obrigatório")
		@Size(max = 255, message = "Nome/razão social deve ter no máximo 255 caracteres")
		String nomeRazaoSocial,

		@Pattern(regexp = "\\d{14}", message = "CNPJ deve ter 14 dígitos, somente números")
		String cnpj,

		@Size(max = 100, message = "Segmento deve ter no máximo 100 caracteres")
		String segmento,

		@Size(max = 50, message = "Tamanho da empresa deve ter no máximo 50 caracteres")
		String tamanhoEmpresa,

		@Pattern(regexp = "\\d{10,11}", message = "Telefone deve ter DDD + número, somente números (10 ou 11 dígitos)")
		String telefone,

		@Email(message = "E-mail inválido")
		@Size(max = 255, message = "E-mail deve ter no máximo 255 caracteres")
		String email,

		@Size(max = 255, message = "Site deve ter no máximo 255 caracteres")
		String site,

		Long usuarioResponsavelId) {
}
