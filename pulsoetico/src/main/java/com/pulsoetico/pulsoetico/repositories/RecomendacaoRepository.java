package com.pulsoetico.pulsoetico.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.AvaliacaoRisco;
import com.pulsoetico.pulsoetico.models.Recomendacao;

public interface RecomendacaoRepository extends JpaRepository<Recomendacao, Long> {

    List<Recomendacao> findByAvaliacaoRisco(AvaliacaoRisco avaliacaoRisco);

    /** Recomendações ainda não vistas pelo gestor/RH — o que aparece como "pendente" no painel. */
    List<Recomendacao> findByAvaliacaoRisco_Setor_IdAndReconhecidaFalse(Long setorId);
}