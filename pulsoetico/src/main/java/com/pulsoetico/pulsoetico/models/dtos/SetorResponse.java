package com.pulsoetico.pulsoetico.models.dtos;

import java.time.Instant;

import com.pulsoetico.pulsoetico.models.Setor;

public record SetorResponse(
        Long id,
        String nome,
        Integer quantidadeColaboradores,
        Double taxaRotatividadeMensal,
        Integer quantidadeDenunciasAnonimasMensal,
        Instant criadoEm
) {
    public static SetorResponse from(Setor setor) {
        return new SetorResponse(
                setor.getId(),
                setor.getNome(),
                setor.getQuantidadeColaboradores(),
                setor.getTaxaRotatividadeMensal(),
                setor.getQuantidadeDenunciasAnonimasMensal(),
                setor.getCriadoEm()
        );
    }
}
