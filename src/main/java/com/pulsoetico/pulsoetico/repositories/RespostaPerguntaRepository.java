package com.pulsoetico.pulsoetico.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.RespostaPergunta;

public interface RespostaPerguntaRepository
        extends JpaRepository<RespostaPergunta, Long> {
}