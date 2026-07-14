package com.pulsoetico.pulsoetico.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.Setor;

public interface SetorRepository extends JpaRepository<Setor, Long> {

    /*
     * Métodos usados pelos setores antigos sem empresa.
     */
    Optional<Setor> findFirstByNomeAndEmpresaIsNull(String nome);

    boolean existsByNomeAndEmpresaIsNull(String nome);

    /*
     * Métodos do novo fluxo.
     */
    Optional<Setor> findByIdAndEmpresaId(
            Long setorId,
            Long empresaId
    );

    Optional<Setor> findByEmpresaIdAndNomeIgnoreCase(
            Long empresaId,
            String nome
    );

    List<Setor> findAllByEmpresaIdOrderByNomeAsc(Long empresaId);

    long countByEmpresaId(Long empresaId);
}