package com.pulsoetico.pulsoetico.repositories;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.Denuncia;
import com.pulsoetico.pulsoetico.models.Setor;

public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {

    long countBySetorAndCriadoEmBetween(Setor setor, Instant inicio, Instant fim);
}
