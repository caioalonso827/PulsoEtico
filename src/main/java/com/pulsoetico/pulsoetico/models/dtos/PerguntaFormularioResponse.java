package com.pulsoetico.pulsoetico.models.dtos;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record PerguntaFormularioResponse(
        @Schema(description = "ID da pergunta que deve ser enviado no POST de respostas", example = "12")
        Long id,

        @Schema(description = "Enunciado da pergunta", example = "Termino minha jornada sentindo-me completamente esgotado.")
        String texto,

        @Schema(description = "Ordem de exibição da pergunta", example = "1")
        Integer ordem,

        @Schema(description = "Alternativas disponíveis para esta pergunta")
        List<AlternativaFormularioResponse> alternativas
) {
}
