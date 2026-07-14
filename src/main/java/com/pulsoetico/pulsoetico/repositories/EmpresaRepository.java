package com.pulsoetico.pulsoetico.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByCodigoConviteIgnoreCase(String codigoConvite);

    boolean existsByCodigoConvite(String codigoConvite);
}