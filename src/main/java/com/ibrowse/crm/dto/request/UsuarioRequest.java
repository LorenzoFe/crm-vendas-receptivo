package com.ibrowse.crm.dto.request;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(

		@NotBlank(message = "Nome é obrigatório")
		@Size(max = 255, message = "Nome deve conter no máximo 255 caracteres")
		String nome,

		@NotBlank(message = "E-mail é obrigatório")
		@Email(message = "E-mail inválido")
		@Size(max = 255, message = "E-mail deve conter no máximo 255 caracteres")
		String email,

		@NotBlank(message = "Senha é obrigatória")
		@Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
		String senha,

		@Size(max = 100, message = "Departamento deve ter no máximo 100 caracteres")
		String departamento,

		@NotEmpty(message = "Informe pelo menos uma role")
		List<String> roles) {
}
