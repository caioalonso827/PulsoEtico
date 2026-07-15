package com.pulsoetico.pulsoetico.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.FormularioModelo;
import com.pulsoetico.pulsoetico.models.TipoFormularioPsicossocial;

public interface FormularioModeloRepository
        extends JpaRepository<FormularioModelo, Long> {

    Optional<FormularioModelo> findByTipoAndAtivoTrue(
            TipoFormularioPsicossocial tipo
    );

    @EntityGraph(attributePaths = "perguntas")
    List<FormularioModelo> findAllByAtivoTrueOrderByTituloAsc();

    boolean existsByTipo(
            TipoFormularioPsicossocial tipo
    );
}
