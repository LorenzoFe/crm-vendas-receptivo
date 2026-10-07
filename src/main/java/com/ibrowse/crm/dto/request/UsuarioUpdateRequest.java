package com.ibrowse.crm.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateRequest(

		@NotBlank(message = "Nome é obrigatório")
		@Size(max = 255, message = "Nome deve conter no máximo 255 caracteres")
		String nome,

		@NotBlank(message = "E-mail é obrigatório")
		@Email(message = "E-mail inválido")
		@Size(max = 255, message = "E-mail deve conter no máximo 255 caracteres")
		String email,

		@Size(max = 100, message = "Departamento deve ter no máximo 100 caracteres")
		String departamento) {
}
