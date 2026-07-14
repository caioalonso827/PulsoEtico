package com.pulsoetico.pulsoetico.models.dtos;


import java.time.Instant;

import com.pulsoetico.pulsoetico.models.Recomendacao;

public record RecomendacaoResponse(
        Long id,
        String mensagem,
        Recomendacao.TipoRecomendacao tipo,
        boolean reconhecida,
        Instant criadoEm
) {
    public static RecomendacaoResponse from(Recomendacao recomendacao) {
        return new RecomendacaoResponse(
                recomendacao.getId(),
                recomendacao.getMensagem(),
                recomendacao.getTipo(),
                recomendacao.isReconhecida(),
                recomendacao.getCriadoEm()
        );
    }
}