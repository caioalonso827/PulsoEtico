package com.pulsoetico.pulsoetico.services;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.MembroEmpresa;
import com.pulsoetico.pulsoetico.models.Permissoes;
import com.pulsoetico.pulsoetico.models.RegistroPonto;
import com.pulsoetico.pulsoetico.models.dtos.RegistroPontoRequest;
import com.pulsoetico.pulsoetico.repositories.MembroEmpresaRepository;
import com.pulsoetico.pulsoetico.repositories.RegistroPontoRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class RegistroPontoService {

    private static final ZoneId ZONA =
            ZoneId.of("America/Sao_Paulo");

    private static final Duration COOLDOWN_ENTRE_REGISTROS =
            Duration.ofMinutes(5);

    private static final RegistroPonto.TipoRegistro[] SEQUENCIA = {
            RegistroPonto.TipoRegistro.ENTRADA,
            RegistroPonto.TipoRegistro.SAIDA
    };

    private final RegistroPontoRepository registroPontoRepository;
    private final MembroEmpresaRepository membroEmpresaRepository;
    private final AutorizacaoEmpresaService autorizacao;

    public RegistroPontoService(
            RegistroPontoRepository registroPontoRepository,
            MembroEmpresaRepository membroEmpresaRepository,
            AutorizacaoEmpresaService autorizacao
    ) {
        this.registroPontoRepository = registroPontoRepository;
        this.membroEmpresaRepository = membroEmpresaRepository;
        this.autorizacao = autorizacao;
    }

    @Transactional
    public RegistroPonto registrar(
            Long empresaId,
            Funcionario funcionario,
            RegistroPontoRequest request
    ) {
        MembroEmpresa membroAutorizado =
                autorizacao.exigirPermissao(
                        empresaId,
                        funcionario,
                        Permissoes.REGISTRAR_PONTO
                );

        /*
         * Bloqueia o vínculo do usuário até o fim da transação. Isso evita que
         * duas requisições simultâneas passem pela validação e criem dois
         * registros quase no mesmo instante.
         */
        membroEmpresaRepository
                .bloquearMembroParaRegistroDePonto(
                        membroAutorizado.getId()
                )
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Vínculo com a empresa não encontrado"
                        )
                );

        MembroEmpresa membro = membroAutorizado;

        if (membro.getSetor() == null) {
            throw new IllegalArgumentException(
                    "Para registrar o ponto, você precisa estar vinculado a um setor"
            );
        }

        Instant agora = Instant.now();

        RegistroPonto ultimoRegistro = registroPontoRepository
                .findTopByEmpresaIdAndFuncionarioIdOrderByHorarioDesc(
                        empresaId,
                        funcionario.getId()
                );

        validarCooldown(ultimoRegistro, agora);

        RegistroPonto.TipoRegistro proximoTipo =
                deduzirProximoTipo(ultimoRegistro);

        RegistroPonto registro = RegistroPonto.builder()
                .funcionario(funcionario)
                .empresa(membro.getEmpresa())
                .setor(membro.getSetor())
                .tipo(proximoTipo)
                .horario(agora)
                .fotoBase64(request.fotoBase64())
                .build();

        return registroPontoRepository.save(registro);
    }

    @Transactional(readOnly = true)
    public List<RegistroPonto> buscarRegistrosDeHoje(
            Long empresaId,
            Funcionario funcionario
    ) {
        autorizacao.exigirMembro(
                empresaId,
                funcionario
        );

        Instant inicioDoDia = LocalDate.now(ZONA)
                .atStartOfDay(ZONA)
                .toInstant();

        Instant fimDoDia =
                inicioDoDia.plusSeconds(86400);

        return registroPontoRepository
                .findByEmpresaIdAndFuncionarioIdAndHorarioBetweenOrderByHorarioAsc(
                        empresaId,
                        funcionario.getId(),
                        inicioDoDia,
                        fimDoDia
                );
    }

    private void validarCooldown(
            RegistroPonto ultimoRegistro,
            Instant agora
    ) {
        if (ultimoRegistro == null) {
            return;
        }

        Instant proximoRegistroPermitidoEm =
                ultimoRegistro.getHorario()
                        .plus(COOLDOWN_ENTRE_REGISTROS);

        if (!agora.isBefore(proximoRegistroPermitidoEm)) {
            return;
        }

        long segundosRestantes = Duration.between(
                agora,
                proximoRegistroPermitidoEm
        ).getSeconds();

        long minutosRestantes = Math.max(
                1,
                (segundosRestantes + 59) / 60
        );

        RegistroPonto.TipoRegistro proximoTipo =
                deduzirProximoTipo(ultimoRegistro);

        String acao = proximoTipo == RegistroPonto.TipoRegistro.SAIDA
                ? "encerrar o ponto"
                : "registrar uma nova entrada";

        throw new IllegalArgumentException(
                "Aguarde 5 minutos entre os registros de ponto. "
                        + "Você poderá "
                        + acao
                        + " em aproximadamente "
                        + minutosRestantes
                        + (minutosRestantes == 1
                                ? " minuto"
                                : " minutos")
                        + "."
        );
    }

    private RegistroPonto.TipoRegistro deduzirProximoTipo(
            RegistroPonto ultimoRegistro
    ) {
        if (ultimoRegistro == null) {
            return RegistroPonto.TipoRegistro.ENTRADA;
        }

        int indiceAtual = indexOf(ultimoRegistro.getTipo());

        int proximoIndice =
                (indiceAtual + 1)
                        % SEQUENCIA.length;

        return SEQUENCIA[proximoIndice];
    }

    private int indexOf(
            RegistroPonto.TipoRegistro tipo
    ) {
        for (int i = 0; i < SEQUENCIA.length; i++) {
            if (SEQUENCIA[i] == tipo) {
                return i;
            }
        }

        return SEQUENCIA.length - 1;
    }
}
