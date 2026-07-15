package com.pulsoetico.pulsoetico.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.RegistroPonto;
import com.pulsoetico.pulsoetico.models.Setor;

public interface RegistroPontoRepository extends JpaRepository<RegistroPonto, Long> {

    List<RegistroPonto>
    findByEmpresaIdAndFuncionarioIdAndHorarioBetweenOrderByHorarioAsc(
            Long empresaId,
            Long funcionarioId,
            Instant inicio,
            Instant fim
    );

    RegistroPonto
    findTopByEmpresaIdAndFuncionarioIdOrderByHorarioDesc(
            Long empresaId,
            Long funcionarioId
    );

    List<RegistroPonto> findBySetorAndHorarioBetween(
            Setor setor,
            Instant inicio,
            Instant fim
    );

    List<RegistroPonto>
    findByEmpresaIdAndHorarioBetweenOrderByHorarioAsc(
            Long empresaId,
            Instant inicio,
            Instant fim
    );
}
