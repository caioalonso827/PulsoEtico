package com.pulsoetico.pulsoetico.models.dtos;

import jakarta.validation.constraints.NotBlank;

public record RegistroPontoRequest(

        @NotBlank(message = "A foto é obrigatória para comprovar o registro")
        String fotoBase64
) {
}
