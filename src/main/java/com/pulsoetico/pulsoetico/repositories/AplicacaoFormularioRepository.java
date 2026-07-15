package com.pulsoetico.pulsoetico.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pulsoetico.pulsoetico.models.AplicacaoFormulario;

public interface AplicacaoFormularioRepository
        extends JpaRepository<AplicacaoFormulario, Long> {

    Optional<AplicacaoFormulario> findByIdAndEmpresaId(
            Long aplicacaoId,
            Long empresaId
    );

    List<AplicacaoFormulario>
    findAllByEmpresaIdOrderByCriadoEmDesc(
            Long empresaId
    );

 @Query("""
        select distinct a
        from AplicacaoFormulario a
        join a.setores setor
        where a.empresa.id = :empresaId
          and setor.id = :setorId
          and a.inicioEm <= :agora
          and a.fimEm > :agora
          and a.canceladoEm is null
          and a.encerradoEm is null
        order by a.fimEm asc
        """)
List<AplicacaoFormulario> buscarDisponiveis(
        @Param("empresaId") Long empresaId,
        @Param("setorId") Long setorId,
        @Param("agora") Instant agora
);
    Optional<AplicacaoFormulario> findByIdAndEmpresa_Id(
        Long aplicacaoId,
        Long empresaId
);

    @Query("""
            SELECT DISTINCT aplicacao
            FROM AplicacaoFormulario aplicacao
            JOIN aplicacao.setores setor
            WHERE aplicacao.empresa.id = :empresaId
              AND setor.id = :setorId
              AND aplicacao.inicioEm <= :agora
              AND aplicacao.fimEm > :agora
              AND aplicacao.canceladoEm IS NULL
              AND aplicacao.encerradoEm IS NULL
            ORDER BY aplicacao.fimEm ASC
            """)
    List<AplicacaoFormulario> encontrarAtivasParaSetor(
            @Param("empresaId") Long empresaId,
            @Param("setorId") Long setorId,
            @Param("agora") Instant agora
    );
}