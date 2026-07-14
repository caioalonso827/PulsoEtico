package com.pulsoetico.pulsoetico.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.RegistroPonto;
import com.pulsoetico.pulsoetico.models.Setor;

public interface RegistroPontoRepository extends JpaRepository<RegistroPonto, Long> {

    /** Registros de um funcionário no dia/período — usado pra montar o "espelho de ponto" dele. */
    List<RegistroPonto> findByFuncionarioAndHorarioBetweenOrderByHorarioAsc(
            Funcionario funcionario, Instant inicio, Instant fim);

    /** Último registro do funcionário — usado pra saber se o próximo toque é ENTRADA ou SAIDA. */
    RegistroPonto findTopByFuncionarioOrderByHorarioDesc(Funcionario funcionario);

    /**
     * Todos os registros de ponto de um SETOR num período — é essa consulta que vai
     * alimentar o cálculo automático de horas extras médias no RiskCalculationService,
     * agregado por setor (nunca exposto por pessoa nos relatórios de risco).
     */
    List<RegistroPonto> findByFuncionario_SetorAndHorarioBetween(Setor setor, Instant inicio, Instant fim);

    /** Todos os registros de ponto da empresa toda (qualquer setor) num período. */
    List<RegistroPonto> findByHorarioBetween(Instant inicio, Instant fim);
}