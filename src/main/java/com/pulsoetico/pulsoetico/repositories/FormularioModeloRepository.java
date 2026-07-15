package com.pulsoetico.pulsoetico.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.FormularioModelo;
import com.pulsoetico.pulsoetico.models.TipoFormularioPsicossocial;

public interface FormularioModeloRepository
        extends JpaRepository<FormularioModelo, Long> {

    Optional<FormularioModelo> findByTipoAndAtivoTrue(
            TipoFormularioPsicossocial tipo
    );

    boolean existsByTipo(
            TipoFormularioPsicossocial tipo
    );
}