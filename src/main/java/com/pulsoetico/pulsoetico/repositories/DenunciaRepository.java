package com.pulsoetico.pulsoetico.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.Denuncia;
import com.pulsoetico.pulsoetico.models.Setor;

public interface DenunciaRepository
        extends JpaRepository<Denuncia, Long> {

    long countBySetorAndCriadoEmBetween(
            Setor setor,
            Instant inicio,
            Instant fim
    );

    long countByStatus(
            Denuncia.StatusDenuncia status
    );

    long countByStatusAndCriadoEmBefore(
            Denuncia.StatusDenuncia status,
            Instant limite
    );

    List<Denuncia> findTop20ByOrderByCriadoEmDesc();

    List<Denuncia>
    findTop20BySetor_Empresa_IdOrderByCriadoEmDesc(
            Long empresaId
    );

    Optional<Denuncia>
    findByIdAndSetor_Empresa_Id(
            Long denunciaId,
            Long empresaId
    );
}