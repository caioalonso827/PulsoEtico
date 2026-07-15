package com.pulsoetico.pulsoetico.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.PerguntaFormulario;

public interface PerguntaFormularioRepository
        extends JpaRepository<PerguntaFormulario, Long> {

    List<PerguntaFormulario>
    findAllByFormularioIdOrderByOrdemAsc(
            Long formularioId
    );
}