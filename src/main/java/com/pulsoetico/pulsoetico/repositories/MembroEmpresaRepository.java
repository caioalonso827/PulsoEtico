package com.pulsoetico.pulsoetico.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.MembroEmpresa;

public interface MembroEmpresaRepository
        extends JpaRepository<MembroEmpresa, Long> {

    Optional<MembroEmpresa> findByEmpresaIdAndFuncionarioId(
            Long empresaId,
            Long funcionarioId
    );

    Optional<MembroEmpresa> findByEmpresaIdAndFuncionarioIdAndAtivoTrue(
            Long empresaId,
            Long funcionarioId
    );

    Optional<MembroEmpresa> findByIdAndEmpresaId(
            Long membroId,
            Long empresaId
    );

    List<MembroEmpresa>
    findAllByFuncionarioIdAndAtivoTrueOrderByEntrouEmDesc(
            Long funcionarioId
    );

    List<MembroEmpresa>
    findAllByEmpresaIdAndAtivoTrueOrderByFuncionarioNomeCompletoAsc(
            Long empresaId
    );

    long countByEmpresaIdAndAtivoTrue(Long empresaId);

    long countByCargoId(Long cargoId);

    long countBySetorIdAndAtivoTrue(Long setorId);
}
