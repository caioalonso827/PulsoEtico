package com.pulsoetico.pulsoetico.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pulsoetico.pulsoetico.models.Funcionario;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    @Query("""
      SELECT f
      FROM Funcionario f
      LEFT JOIN FETCH f.setor
      WHERE LOWER(f.email) = LOWER(:email)
      """)
    Optional<Funcionario> findByEmailWithSetor(@Param("email") String email);

    boolean existsByEmail(String email);

    boolean existsByMatricula(String matricula);


    Optional<Funcionario> findByEmail(String email);

    long countByAtivoTrue();

    List<Funcionario> findByAtivoTrue();
}
