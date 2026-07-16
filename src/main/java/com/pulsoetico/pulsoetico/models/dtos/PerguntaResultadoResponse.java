package com.pulsoetico.pulsoetico.models.dtos;

import java.util.List;

public record PerguntaResultadoResponse(
        Long perguntaId,
        String texto,
        Integer ordem,
        long quantidadeRespostas,
        double media,
        List<DistribuicaoRespostaResponse> distribuicao
) {
}
