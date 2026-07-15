package com.pulsoetico.pulsoetico.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.AvaliacaoRisco;
import com.pulsoetico.pulsoetico.models.Recomendacao;

public interface RecomendacaoRepository
        extends JpaRepository<Recomendacao, Long> {

    List<Recomendacao> findByAvaliacaoRisco(
            AvaliacaoRisco avaliacaoRisco
    );

    List<Recomendacao>
    findByAvaliacaoRisco_Setor_IdAndAvaliacaoRisco_Setor_Empresa_IdAndReconhecidaFalse(
            Long setorId,
            Long empresaId
    );

    Optional<Recomendacao>
    findByIdAndAvaliacaoRisco_Setor_Empresa_Id(
            Long recomendacaoId,
            Long empresaId
    );

    List<Recomendacao>
    findTop20ByAvaliacaoRisco_Setor_Empresa_IdOrderByCriadoEmDesc(
            Long empresaId
    );
}
