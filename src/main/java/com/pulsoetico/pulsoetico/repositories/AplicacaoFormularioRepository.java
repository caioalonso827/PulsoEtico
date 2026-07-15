package com.pulsoetico.pulsoetico.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pulsoetico.pulsoetico.models.AplicacaoFormulario;

public interface AplicacaoFormularioRepository
        extends JpaRepository<AplicacaoFormulario, Long> {

    Optional<AplicacaoFormulario> findByIdAndEmpresaId(
            Long aplicacaoId,
            Long empresaId
    );

    List<AplicacaoFormulario>
    findAllByEmpresaIdOrderByCriadoEmDesc(
            Long empresaId
    );

    @Query("""
        SELECT DISTINCT a
        FROM AplicacaoFormulario a
        JOIN a.setores s
        WHERE a.empresa.id = :empresaId
          AND s.id = :setorId
          AND a.canceladoEm IS NULL
          AND a.inicioEm <= :agora
          AND a.fimEm >= :agora
        ORDER BY a.fimEm ASC
        """)
    List<AplicacaoFormulario> encontrarAtivasParaSetor(
            @Param("empresaId") Long empresaId,
            @Param("setorId") Long setorId,
            @Param("agora") Instant agora
    );
}