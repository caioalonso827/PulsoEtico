package com.pulsoetico.pulsoetico.services;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.Setor;
import com.pulsoetico.pulsoetico.repositories.MembroEmpresaRepository;

@Service
public class RotatividadeService {

    private final MembroEmpresaRepository membroRepository;

    public RotatividadeService(
            MembroEmpresaRepository membroRepository
    ) {
        this.membroRepository = membroRepository;
    }

    @Transactional(readOnly = true)
    public double calcularTaxaRotatividade(
            Setor setor,
            Instant inicio,
            Instant fim
    ) {
        long desligados = membroRepository
                .countBySetorAndSaiuEmBetween(
                        setor,
                        inicio,
                        fim
                );

        long ativosInicio = membroRepository
                .contarAtivosNoMomento(
                        setor,
                        inicio
                );

        long ativosFim = membroRepository
                .contarAtivosNoMomento(
                        setor,
                        fim
                );

        double mediaAtivos =
                (ativosInicio + ativosFim) / 2.0;

        if (mediaAtivos <= 0) {
            return 0.0;
        }

        double taxa =
                (desligados / mediaAtivos) * 100.0;

        return Math.round(taxa * 100.0) / 100.0;
    }
}