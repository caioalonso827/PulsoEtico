package com.pulsoetico.pulsoetico.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.CheckinHumor;
import com.pulsoetico.pulsoetico.models.ControleCheckinHumorDiario;
import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.MembroEmpresa;
import com.pulsoetico.pulsoetico.models.Permissoes;
import com.pulsoetico.pulsoetico.models.dtos.CheckinHumorRequest;
import com.pulsoetico.pulsoetico.models.dtos.CheckinHumorStatusResponse;
import com.pulsoetico.pulsoetico.repositories.CheckinHumorRepository;
import com.pulsoetico.pulsoetico.repositories.ControleCheckinHumorDiarioRepository;
import com.pulsoetico.pulsoetico.repositories.MembroEmpresaRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class MoodCheckinService {

    private static final ZoneId ZONA =
            ZoneId.of("America/Sao_Paulo");

    private final CheckinHumorRepository checkinHumorRepository;
    private final ControleCheckinHumorDiarioRepository controleRepository;
    private final MembroEmpresaRepository membroEmpresaRepository;
    private final AutorizacaoEmpresaService autorizacao;

    public MoodCheckinService(
            CheckinHumorRepository checkinHumorRepository,
            ControleCheckinHumorDiarioRepository controleRepository,
            MembroEmpresaRepository membroEmpresaRepository,
            AutorizacaoEmpresaService autorizacao
    ) {
        this.checkinHumorRepository = checkinHumorRepository;
        this.controleRepository = controleRepository;
        this.membroEmpresaRepository = membroEmpresaRepository;
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

        /*
         * O bloqueio é feito na linha do vínculo do usuário. Assim, duas
         * requisições simultâneas do mesmo usuário não conseguem passar juntas
         * pela verificação diária.
         */
        membroEmpresaRepository
                .bloquearMembroParaCheckinHumor(membro.getId())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Vínculo com a empresa não encontrado"
                        )
                );

        LocalDate hoje = LocalDate.now(ZONA);

        boolean jaRespondeuHoje = controleRepository
                .existsByEmpresa_IdAndFuncionario_IdAndDataCheckin(
                        empresaId,
                        funcionarioLogado.getId(),
                        hoje
                );

        if (jaRespondeuHoje) {
            throw new IllegalArgumentException(
                    "Você já realizou o check-in de humor hoje. "
                            + "Um novo check-in estará disponível amanhã."
            );
        }

        /*
         * Esta tabela guarda apenas empresa, usuário e data. Ela não guarda o
         * nível escolhido e não se relaciona com a resposta anônima.
         */
        ControleCheckinHumorDiario controle =
                ControleCheckinHumorDiario.builder()
                        .empresa(membro.getEmpresa())
                        .funcionario(funcionarioLogado)
                        .dataCheckin(hoje)
                        .build();

        controleRepository.save(controle);

        CheckinHumor checkin = CheckinHumor.builder()
                .setor(membro.getSetor())
                .nivelHumor(request.nivelHumor())
                .build();

        return checkinHumorRepository.save(checkin);
    }

    @Transactional(readOnly = true)
    public CheckinHumorStatusResponse consultarStatusHoje(
            Funcionario funcionarioLogado,
            Long empresaId
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

        LocalDate hoje = LocalDate.now(ZONA);

        boolean respondidoHoje = controleRepository
                .existsByEmpresa_IdAndFuncionario_IdAndDataCheckin(
                        empresaId,
                        funcionarioLogado.getId(),
                        hoje
                );

        Instant proximoCheckinEm = respondidoHoje
                ? hoje.plusDays(1)
                        .atStartOfDay(ZONA)
                        .toInstant()
                : null;

        return new CheckinHumorStatusResponse(
                !respondidoHoje,
                respondidoHoje,
                hoje,
                proximoCheckinEm
        );
    }
}
