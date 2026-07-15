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

    long countBySetor_Empresa_IdAndStatus(
            Long empresaId,
            Denuncia.StatusDenuncia status
    );

    long countBySetor_Empresa_IdAndStatusAndCriadoEmBefore(
            Long empresaId,
            Denuncia.StatusDenuncia status,
            Instant limite
    );

    List<Denuncia>
    findTop20BySetor_Empresa_IdOrderByCriadoEmDesc(
            Long empresaId
    );

    Optional<Denuncia>
    findByIdAndSetor_Empresa_Id(
            Long denunciaId,
            Long empresaId
    );

    List<Denuncia> findAllBySetor_Empresa_Id(
        Long empresaId
);
}