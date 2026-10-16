package com.ibrowse.crm.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MudarEstagioRequest(@NotNull @Positive Long estagioId) {
}
