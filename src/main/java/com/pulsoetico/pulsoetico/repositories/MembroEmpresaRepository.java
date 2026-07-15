package com.pulsoetico.pulsoetico.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pulsoetico.pulsoetico.models.MembroEmpresa;
import com.pulsoetico.pulsoetico.models.Setor;

public interface MembroEmpresaRepository
        extends JpaRepository<MembroEmpresa, Long> {

    Optional<MembroEmpresa>
    findByEmpresaIdAndFuncionarioIdAndAtivoTrue(
            Long empresaId,
            Long funcionarioId
    );

    Optional<MembroEmpresa> findByIdAndEmpresaId(
            Long membroId,
            Long empresaId
    );

    @Query(
            value = "SELECT id FROM membros_empresa WHERE id = :membroId FOR UPDATE",
            nativeQuery = true
    )
    Optional<Long> bloquearMembroParaRegistroDePonto(
            @Param("membroId") Long membroId
    );

    List<MembroEmpresa>
    findAllByFuncionarioIdAndAtivoTrueOrderByEntrouEmDesc(
            Long funcionarioId
    );

    Optional<MembroEmpresa>
    findFirstByFuncionarioIdAndAtivoTrueOrderByEntrouEmAsc(
            Long funcionarioId
    );

    List<MembroEmpresa>
    findAllByEmpresaIdAndAtivoTrueOrderByFuncionarioNomeCompletoAsc(
            Long empresaId
    );

    List<MembroEmpresa> findAllByEmpresaIdAndSetorIsNull(
            Long empresaId
    );

    long countByEmpresaIdAndAtivoTrue(
            Long empresaId
    );

    long countByCargoIdAndAtivoTrue(
            Long cargoId
    );

    long countBySetorIdAndAtivoTrue(
            Long setorId
    );

    long countBySetorAndSaiuEmBetween(
            Setor setor,
            Instant inicio,
            Instant fim
    );

    @Query("""
            SELECT COUNT(m)
            FROM MembroEmpresa m
            WHERE m.setor = :setor
              AND m.entrouEm <= :momento
              AND (
                    m.saiuEm IS NULL
                    OR m.saiuEm > :momento
              )
            """)
    long contarAtivosNoMomento(
            @Param("setor") Setor setor,
            @Param("momento") Instant momento
    );

    Optional<MembroEmpresa> findByEmpresa_IdAndFuncionario_Id(
        Long empresaId,
        Long funcionarioId
);

List<MembroEmpresa> findAllByEmpresaId(
        Long empresaId
);
}