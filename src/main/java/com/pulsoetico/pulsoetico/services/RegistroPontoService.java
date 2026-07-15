package com.pulsoetico.pulsoetico.services;

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
import com.pulsoetico.pulsoetico.repositories.RegistroPontoRepository;

@Service
public class RegistroPontoService {

    private static final ZoneId ZONA =
            ZoneId.of("America/Sao_Paulo");

    private static final RegistroPonto.TipoRegistro[] SEQUENCIA = {
            RegistroPonto.TipoRegistro.ENTRADA,
            RegistroPonto.TipoRegistro.SAIDA
    };

    private final RegistroPontoRepository registroPontoRepository;
    private final AutorizacaoEmpresaService autorizacao;

    public RegistroPontoService(
            RegistroPontoRepository registroPontoRepository,
            AutorizacaoEmpresaService autorizacao
    ) {
        this.registroPontoRepository =
                registroPontoRepository;

        this.autorizacao = autorizacao;
    }

    @Transactional
    public RegistroPonto registrar(
            Long empresaId,
            Funcionario funcionario,
            RegistroPontoRequest request
    ) {
        MembroEmpresa membro =
                autorizacao.exigirPermissao(
                        empresaId,
                        funcionario,
                        Permissoes.REGISTRAR_PONTO
                );
if (membro.getSetor() == null) {
    throw new IllegalArgumentException(
            "Para registrar o ponto, você precisa estar vinculado a um setor"
    );
}

        RegistroPonto.TipoRegistro proximoTipo =
                deduzirProximoTipo(
                        empresaId,
                        funcionario.getId()
                );

        RegistroPonto registro = RegistroPonto.builder()
                .funcionario(funcionario)
                .empresa(membro.getEmpresa())
                .setor(membro.getSetor())
                .tipo(proximoTipo)
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

    private RegistroPonto.TipoRegistro deduzirProximoTipo(
            Long empresaId,
            Long funcionarioId
    ) {
        RegistroPonto ultimo =
                registroPontoRepository
                        .findTopByEmpresaIdAndFuncionarioIdOrderByHorarioDesc(
                                empresaId,
                                funcionarioId
                        );

        if (ultimo == null) {
            return RegistroPonto.TipoRegistro.ENTRADA;
        }

        int indiceAtual = indexOf(ultimo.getTipo());

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