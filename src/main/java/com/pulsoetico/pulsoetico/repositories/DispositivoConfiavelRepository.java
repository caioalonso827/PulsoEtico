package com.pulsoetico.pulsoetico.repositories;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.DispositivoConfiavel;

public interface DispositivoConfiavelRepository extends JpaRepository<DispositivoConfiavel, Long> {

    Optional<DispositivoConfiavel> findByTokenHashAndFuncionarioIdAndExpiraEmAfter(
            String tokenHash, Long funcionarioId, Instant agora);
}
