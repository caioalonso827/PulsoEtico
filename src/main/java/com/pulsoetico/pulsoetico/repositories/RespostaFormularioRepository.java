package com.pulsoetico.pulsoetico.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsoetico.pulsoetico.models.RespostaFormulario;

public interface RespostaFormularioRepository
        extends JpaRepository<RespostaFormulario, Long> {

    boolean existsByAplicacaoIdAndMembroId(
            Long aplicacaoId,
            Long membroId
    );

    long countByAplicacaoId(Long aplicacaoId);

    List<RespostaFormulario>
    findAllByAplicacaoId(Long aplicacaoId);

    List<RespostaFormulario> findAllByAplicacao_Empresa_Id(
        Long empresaId
);
}