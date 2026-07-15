package com.pulsoetico.pulsoetico.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.Funcionario;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

  Optional<Funcionario> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    boolean existsByMatricula(String matricula);


    Optional<Funcionario> findByEmail(String email);

    long countByAtivoTrue();

    List<Funcionario> findByAtivoTrue();
}
