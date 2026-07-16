package com.pulsoetico.pulsoetico.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.ControleCheckinHumorDiario;

public interface ControleCheckinHumorDiarioRepository
        extends JpaRepository<ControleCheckinHumorDiario, Long> {

    boolean existsByEmpresa_IdAndFuncionario_IdAndDataCheckin(
            Long empresaId,
            Long funcionarioId,
            LocalDate dataCheckin
    );

    List<ControleCheckinHumorDiario> findAllByEmpresa_Id(
            Long empresaId
    );
}
