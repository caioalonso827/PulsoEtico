package com.pulsoetico.pulsoetico.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.Denuncia;
import com.pulsoetico.pulsoetico.models.Setor;

public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {

    long countBySetorAndCriadoEmBetween(Setor setor, Instant inicio, Instant fim);

    long countByStatus(Denuncia.StatusDenuncia status);

    /** "Sem resposta há 48h" = ainda ABERTA e criada há mais tempo que o limite informado. */
    long countByStatusAndCriadoEmBefore(Denuncia.StatusDenuncia status, Instant limite);

    List<Denuncia> findTop20ByOrderByCriadoEmDesc();
}
