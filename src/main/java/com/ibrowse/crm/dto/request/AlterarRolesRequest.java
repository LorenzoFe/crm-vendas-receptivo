package com.ibrowse.crm.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AlterarRolesRequest(

        @NotEmpty(message = "Informe pelo menos uma role")
        List<String> roles) {
}
