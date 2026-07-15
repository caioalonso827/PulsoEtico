package com.pulsoetico.pulsoetico.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pulsoetico.pulsoetico.models.AvaliacaoRisco;
import com.pulsoetico.pulsoetico.models.Setor;

public interface AvaliacaoRiscoRepository
        extends JpaRepository<AvaliacaoRisco, Long> {

    Optional<AvaliacaoRisco>
    findTopBySetorOrderByCalculadoEmDesc(Setor setor);

    List<AvaliacaoRisco>
    findBySetorOrderByCalculadoEmDesc(Setor setor);

    @Query("""
        SELECT a
        FROM AvaliacaoRisco a
        WHERE a.setor.empresa.id = :empresaId
          AND a.calculadoEm = (
              SELECT MAX(a2.calculadoEm)
              FROM AvaliacaoRisco a2
              WHERE a2.setor = a.setor
          )
        """)
    List<AvaliacaoRisco> buscarUltimaAvaliacaoDeCadaSetorDaEmpresa(
            @Param("empresaId") Long empresaId
    );
}
