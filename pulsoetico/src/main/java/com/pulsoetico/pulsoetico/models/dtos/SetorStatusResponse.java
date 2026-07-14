package com.pulsoetico.pulsoetico.models.dtos;

import java.util.List;

/**
 * "Score de bem-estar" é o inverso do índice de risco (100 - índiceRisco),
 * só pra ficar mais intuitivo no dashboard (quanto maior, melhor).
 */
public record SetorStatusResponse(
        Long setorId,
        String setorNome,
        Integer quantidadeColaboradores,
        Double scoreBemEstar,
        String statusLabel,
        List<Double> tendenciaBemEstar
) {
    public enum StatusLabel {
        ESTAVEL, ATENCAO, CRITICO
    }
}
