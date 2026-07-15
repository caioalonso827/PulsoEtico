package com.pulsoetico.pulsoetico.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.CheckinHumor;
import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.MembroEmpresa;
import com.pulsoetico.pulsoetico.models.Permissoes;
import com.pulsoetico.pulsoetico.models.dtos.CheckinHumorRequest;
import com.pulsoetico.pulsoetico.repositories.CheckinHumorRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class MoodCheckinService {

    private final CheckinHumorRepository checkinHumorRepository;
    private final AutorizacaoEmpresaService autorizacao;

    public MoodCheckinService(
            CheckinHumorRepository checkinHumorRepository,
            AutorizacaoEmpresaService autorizacao
    ) {
        this.checkinHumorRepository = checkinHumorRepository;
        this.autorizacao = autorizacao;
    }

    @Transactional
    public CheckinHumor registrar(
            Funcionario funcionarioLogado,
            Long empresaId,
            CheckinHumorRequest request
    ) {
        MembroEmpresa membro = autorizacao.exigirPermissao(
                empresaId,
                funcionarioLogado,
                Permissoes.RESPONDER_PESQUISAS
        );

        if (membro.getSetor() == null) {
            throw new EntityNotFoundException(
                    "Você ainda não possui setor nesta empresa"
            );
        }

        CheckinHumor checkin = CheckinHumor.builder()
                .setor(membro.getSetor())
                .nivelHumor(request.nivelHumor())
                .build();

        return checkinHumorRepository.save(checkin);
    }
}
