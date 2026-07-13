package com.pulsoetico.pulsoetico.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pulsoetico.pulsoetico.models.Funcionario;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    @Query("SELECT f FROM Funcionario f JOIN FETCH f.setor WHERE f.email = :email")
    Optional<Funcionario> findByEmailWithSetor(@Param("email") String email);

    boolean existsByEmail(String email);

    boolean existsByMatricula(String matricula);
}