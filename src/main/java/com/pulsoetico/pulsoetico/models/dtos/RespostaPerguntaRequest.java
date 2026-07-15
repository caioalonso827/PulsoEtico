package com.pulsoetico.pulsoetico.models.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RespostaPerguntaRequest(

        @NotNull(
                message = "O ID da pergunta é obrigatório"
        )
        Long perguntaId,

        @NotNull(
                message = "O valor da resposta é obrigatório"
        )
        @Min(
                value = 1,
                message = "O valor mínimo é 1"
        )
        @Max(
                value = 5,
                message = "O valor máximo é 5"
        )
        Integer valor

) {
}