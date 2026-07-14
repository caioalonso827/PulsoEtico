package com.pulsoetico.pulsoetico.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.Cargo;

public interface CargoRepository extends JpaRepository<Cargo, Long> {

    List<Cargo> findAllByEmpresaIdOrderBySistemaDescNomeAsc(Long empresaId);

    Optional<Cargo> findByIdAndEmpresaId(Long id, Long empresaId);

    Optional<Cargo> findByEmpresaIdAndNomeIgnoreCase(
            Long empresaId,
            String nome
    );

    boolean existsByEmpresaIdAndNomeIgnoreCase(
            Long empresaId,
            String nome
    );
}