package com.pulsoetico.pulsoetico.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.Permissoes;
import com.pulsoetico.pulsoetico.models.Setor;
import com.pulsoetico.pulsoetico.models.dtos.IndicadoresManuaisRequest;
import com.pulsoetico.pulsoetico.repositories.SetorRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SetorService {

    private final SetorRepository setorRepository;
    private final AutorizacaoEmpresaService autorizacao;

    public SetorService(
            SetorRepository setorRepository,
            AutorizacaoEmpresaService autorizacao
    ) {
        this.setorRepository = setorRepository;
        this.autorizacao = autorizacao;
    }

    @Transactional(readOnly = true)
    public Setor buscarPorIdDaEmpresa(
            Long empresaId,
            Long setorId
    ) {
        return setorRepository
                .findByIdAndEmpresaId(
                        setorId,
                        empresaId
                )
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Setor não encontrado nesta empresa"
                        )
                );
    }

    @Transactional
    public Setor atualizarIndicadoresManuais(
            Long empresaId,
            Long setorId,
            IndicadoresManuaisRequest request,
            Funcionario usuario
    ) {
        autorizacao.exigirPermissao(
                empresaId,
                usuario,
                Permissoes.GERENCIAR_SETORES
        );

        Setor setor = buscarPorIdDaEmpresa(
                empresaId,
                setorId
        );

        setor.setTaxaRotatividadeMensal(
                request.taxaRotatividadeMensal()
        );

        setor.setQuantidadeDenunciasAnonimasMensal(
                request.quantidadeDenunciasAnonimasMensal()
        );

        return setorRepository.save(setor);
    }
}