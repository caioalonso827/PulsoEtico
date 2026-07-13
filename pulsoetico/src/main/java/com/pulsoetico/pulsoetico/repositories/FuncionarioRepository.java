package com.pulsoetico.pulsoetico.repositories;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.Setor;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    @Query("SELECT f FROM Funcionario f JOIN FETCH f.setor WHERE f.email = :email")
    Optional<Funcionario> findByEmailWithSetor(@Param("email") String email);

    boolean existsByEmail(String email);

    boolean existsByMatricula(String matricula);

    long countBySetorAndDesligadoEmBetween(Setor setor, Instant inicio, Instant fim);

    @Query("""
            SELECT COUNT(f) FROM Funcionario f
            WHERE f.setor = :setor
              AND f.criadoEm <= :momento
              AND (f.desligadoEm IS NULL OR f.desligadoEm > :momento)
            """)
    long contarAtivosNoMomento(@Param("setor") Setor setor, @Param("momento") Instant momento);
}
