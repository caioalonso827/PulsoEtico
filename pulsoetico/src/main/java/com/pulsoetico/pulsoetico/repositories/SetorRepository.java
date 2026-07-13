package com.pulsoetico.pulsoetico.repositories;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.Setor;

public interface SetorRepository extends JpaRepository<Setor, Long> {

    Optional<Setor> findByNome(String nome);

    boolean existsByNome(String nome);
}
