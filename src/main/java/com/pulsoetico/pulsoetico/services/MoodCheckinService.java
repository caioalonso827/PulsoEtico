package com.pulsoetico.pulsoetico.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.CheckinHumor;
import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.MembroEmpresa;
import com.pulsoetico.pulsoetico.models.dtos.CheckinHumorRequest;
import com.pulsoetico.pulsoetico.repositories.CheckinHumorRepository;
import com.pulsoetico.pulsoetico.repositories.MembroEmpresaRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Registra o check-in de humor. O registro salvo (CheckinHumor) não guarda
 * nenhum identificador de pessoa, só o setor.
 *
 * Correção de bug: o setor vem do MembroEmpresa (empresa ativa do token +
 * funcionário), não de Funcionario.getSetor(). O campo Funcionario.setor é
 * legado de quando só existia uma empresa por pessoa — como alguém pode
 * participar de várias empresas, cada uma com seu próprio setor pra essa
 * pessoa, só o MembroEmpresa da empresa ATIVA tem a resposta certa.
 */
@Service
public class MoodCheckinService {

    private final CheckinHumorRepository checkinHumorRepository;
    private final MembroEmpresaRepository membroEmpresaRepository;

    public MoodCheckinService(
            CheckinHumorRepository checkinHumorRepository,
            MembroEmpresaRepository membroEmpresaRepository
    ) {
        this.checkinHumorRepository = checkinHumorRepository;
        this.membroEmpresaRepository = membroEmpresaRepository;
    }

    @Transactional
    public CheckinHumor registrar(Funcionario funcionarioLogado, Long empresaIdAtual, CheckinHumorRequest request) {
        if (empresaIdAtual == null) {
            throw new IllegalStateException(
                    "Nenhuma empresa ativa no token — selecione uma empresa antes de fazer o check-in");
        }

        MembroEmpresa membro = membroEmpresaRepository
                .findByEmpresaIdAndFuncionarioIdAndAtivoTrue(empresaIdAtual, funcionarioLogado.getId())
                .orElseThrow(() -> new EntityNotFoundException("Você não é membro ativo dessa empresa"));

        if (membro.getSetor() == null) {
            throw new EntityNotFoundException(
                    "Você ainda não foi colocado em um setor nessa empresa — fale com o administrador");
        }

        CheckinHumor checkin = CheckinHumor.builder()
                .setor(membro.getSetor())
                .nivelHumor(request.nivelHumor())
                .build();

        return checkinHumorRepository.save(checkin);
    }
}