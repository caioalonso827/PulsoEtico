package com.pulsoetico.pulsoetico.models.dtos;

public record DistribuicaoRespostaResponse(
        Integer valor,
        String alternativa,
        long quantidade,
        double percentual
) {
}
