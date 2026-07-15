package com.pulsoetico.pulsoetico.models.dtos;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


public record RespostaItemRequest(
        @NotNull
        Long perguntaId,

        @NotNull
        @Min(1)
        @Max(5)
        Integer valor
) {
}