package com.pulsoetico.pulsoetico.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.CodigoVerificacao;

public interface CodigoVerificacaoRepository
        extends JpaRepository<CodigoVerificacao, Long> {

    Optional<CodigoVerificacao> findByCodigo(String codigo);

    Optional<CodigoVerificacao> findTopByEmailOrderByExpiraEmDesc(
            String email);

}