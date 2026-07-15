package com.pulsoetico.pulsoetico.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

public record AlternativaFormularioResponse(
        @Schema(description = "Valor enviado ao responder a pergunta", example = "1")
        Integer valor,

        @Schema(description = "Texto apresentado ao usuário", example = "Nunca")
        String texto
) {
}
