package com.pulsoetico.pulsoetico.repositories;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.AvaliacaoRisco;
import com.pulsoetico.pulsoetico.models.Setor;

public interface AvaliacaoRiscoRepository extends JpaRepository<AvaliacaoRisco, Long> {

    /** Última avaliação calculada para o setor (o "estado atual" mostrado no dashboard). */
    Optional<AvaliacaoRisco> findTopBySetorOrderByCalculadoEmDesc(Setor setor);

    /** Histórico ordenado — usado no gráfico de tendência do painel. */
    List<AvaliacaoRisco> findBySetorOrderByCalculadoEmDesc(Setor setor);

    /** Última avaliação de cada setor, para montar o mapa de risco geral da empresa. */
    @org.springframework.data.jpa.repository.Query("""
        SELECT a FROM AvaliacaoRisco a
        WHERE a.calculadoEm = (
            SELECT MAX(a2.calculadoEm) FROM AvaliacaoRisco a2 WHERE a2.setor = a.setor
        )
        """)
    List<AvaliacaoRisco> buscarUltimaAvaliacaoDeCadaSetor();
}