package com.pulsoetico.pulsoetico.models.dtos;


import java.time.Instant;

import com.pulsoetico.pulsoetico.models.AvaliacaoRisco;

/**
 * Resposta com o índice de risco calculado + os componentes que entraram no
 * cálculo. Expor os componentes aqui é o que dá "explicabilidade" no painel:
 * o gestor vê não só a nota final, mas o porquê dela.
 */
public record AvaliacaoRiscoResponse(
        Long id,
        Long setorId,
        String setorNome,
        Double indiceRisco,
        AvaliacaoRisco.NivelRisco nivelRisco,
        Double mediaHorasExtras,
        Double mediaSeveridadeHumor,
        Double taxaRotatividade,
        Integer quantidadeDenunciasAnonimas,
        Instant calculadoEm
) {
    public static AvaliacaoRiscoResponse from(AvaliacaoRisco avaliacao) {
        return new AvaliacaoRiscoResponse(
                avaliacao.getId(),
                avaliacao.getSetor().getId(),
                avaliacao.getSetor().getNome(),
                avaliacao.getIndiceRisco(),
                avaliacao.getNivelRisco(),
                avaliacao.getMediaHorasExtras(),
                avaliacao.getMediaSeveridadeHumor(),
                avaliacao.getTaxaRotatividade(),
                avaliacao.getQuantidadeDenunciasAnonimas(),
                avaliacao.getCalculadoEm()
        );
    }
}