package com.pulsoetico.pulsoetico.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.CheckinHumor;
import com.pulsoetico.pulsoetico.models.Setor;

public interface CheckinHumorRepository extends JpaRepository<CheckinHumor, Long> {

    /**
     * Todos os check-ins de um setor a partir de uma data (ex: últimos 20 dias).
     * A média de severidade é calculada no service (RiskCalculationService), usando
     * CheckinHumor.NivelHumor#getSeveridade() — mais simples e legível do que fazer
     * o mapeamento enum -> número dentro de uma query JPQL.
     */
    List<CheckinHumor> findBySetorAndCriadoEmAfter(Setor setor, Instant desde);

    long countBySetorAndCriadoEmAfter(Setor setor, Instant desde);
}
