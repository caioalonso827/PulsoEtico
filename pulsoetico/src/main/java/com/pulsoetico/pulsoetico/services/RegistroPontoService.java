package com.pulsoetico.pulsoetico.services;

import com.pulsoetico.pulsoetico.models.dtos.RegistroPontoRequest;
import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.RegistroPonto;
import com.pulsoetico.pulsoetico.repositories.RegistroPontoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class RegistroPontoService {

    private static final RegistroPonto.TipoRegistro[] SEQUENCIA = {
            RegistroPonto.TipoRegistro.ENTRADA,
            RegistroPonto.TipoRegistro.SAIDA
    };

    private final RegistroPontoRepository registroPontoRepository;

    public RegistroPontoService(RegistroPontoRepository registroPontoRepository) {
        this.registroPontoRepository = registroPontoRepository;
    }

    @Transactional
    public RegistroPonto registrar(Funcionario funcionario, RegistroPontoRequest request) {
        RegistroPonto.TipoRegistro proximoTipo = deduzirProximoTipo(funcionario);

        RegistroPonto registro = RegistroPonto.builder()
                .funcionario(funcionario)
                .tipo(proximoTipo)
                .fotoBase64(request.fotoBase64())
                .build();

        return registroPontoRepository.save(registro);
    }

    public List<RegistroPonto> buscarRegistrosDeHoje(Funcionario funcionario) {
        Instant inicioDoDia = LocalDate.now(ZoneId.systemDefault())
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
        Instant fimDoDia = inicioDoDia.plusSeconds(86400);

        return registroPontoRepository
                .findByFuncionarioAndHorarioBetweenOrderByHorarioAsc(funcionario, inicioDoDia, fimDoDia);
    }

    private RegistroPonto.TipoRegistro deduzirProximoTipo(Funcionario funcionario) {
        RegistroPonto ultimo = registroPontoRepository.findTopByFuncionarioOrderByHorarioDesc(funcionario);

        if (ultimo == null) {
            return RegistroPonto.TipoRegistro.ENTRADA;
        }

        int indiceAtual = indexOf(ultimo.getTipo());
        int proximoIndice = (indiceAtual + 1) % SEQUENCIA.length;
        return SEQUENCIA[proximoIndice];
    }

    private int indexOf(RegistroPonto.TipoRegistro tipo) {
        for (int i = 0; i < SEQUENCIA.length; i++) {
            if (SEQUENCIA[i] == tipo) {
                return i;
            }
        }
        return SEQUENCIA.length - 1;
    }
}