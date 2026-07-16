package com.pulsoetico.pulsoetico.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pulsoetico.pulsoetico.models.RespostaFormulario;

public interface RespostaFormularioRepository
        extends JpaRepository<RespostaFormulario, Long> {

    boolean existsByAplicacaoIdAndMembroId(
            Long aplicacaoId,
            Long membroId
    );

    long countByAplicacaoId(Long aplicacaoId);

    List<RespostaFormulario> findAllByAplicacaoId(
            Long aplicacaoId
    );

    @Query("""
            SELECT DISTINCT resposta
            FROM RespostaFormulario resposta
            LEFT JOIN FETCH resposta.respostas item
            LEFT JOIN FETCH item.pergunta
            WHERE resposta.aplicacao.id = :aplicacaoId
            ORDER BY resposta.respondidoEm ASC
            """)
    List<RespostaFormulario> findAllComItensByAplicacaoId(
            @Param("aplicacaoId") Long aplicacaoId
    );

    List<RespostaFormulario> findAllByAplicacao_Empresa_Id(
            Long empresaId
    );
}
