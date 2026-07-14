package com.pulsoetico.pulsoetico.services;

import com.pulsoetico.pulsoetico.models.dtos.FuncionarioRequest;
import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.Setor;
import com.pulsoetico.pulsoetico.repositories.FuncionarioRepository;
import com.pulsoetico.pulsoetico.repositories.SetorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final SetorRepository setorRepository;
    private final PasswordEncoder passwordEncoder;

    public FuncionarioService(
            FuncionarioRepository funcionarioRepository,
            SetorRepository setorRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.funcionarioRepository = funcionarioRepository;
        this.setorRepository = setorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Funcionario criar(FuncionarioRequest request) {
        if (funcionarioRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Já existe um funcionário com esse email: " + request.email());
        }

        Setor setor = setorRepository.findById(request.setorId())
                .orElseThrow(() -> new EntityNotFoundException("Setor não encontrado: " + request.setorId()));

        Funcionario funcionario = Funcionario.builder()
                .nomeCompleto(request.nomeCompleto())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .matricula(request.matricula())
                .papel(request.papel())
                .setor(setor)
                .build();

        return funcionarioRepository.save(funcionario);
    }

    public List<Funcionario> listarTodos() {
        return funcionarioRepository.findAll();
    }

    @Transactional
    public Funcionario desligar(Long funcionarioId, Instant dataDesligamento) {
        Funcionario funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado: " + funcionarioId));

        if (!funcionario.isAtivo() || funcionario.getDesligadoEm() != null) {
            throw new IllegalArgumentException("O funcionário já está desligado");
        }

        Instant desligadoEm = dataDesligamento != null ? dataDesligamento : Instant.now();
        if (desligadoEm.isBefore(funcionario.getCriadoEm())) {
            throw new IllegalArgumentException("A data de desligamento não pode ser anterior à admissão");
        }
        if (desligadoEm.isAfter(Instant.now())) {
            throw new IllegalArgumentException("A data de desligamento não pode estar no futuro");
        }

        funcionario.setAtivo(false);
        funcionario.setDesligadoEm(desligadoEm);
        return funcionarioRepository.save(funcionario);
    }

    public double calcularTaxaRotatividade(Setor setor, Instant inicio, Instant fim) {
        long desligados = funcionarioRepository.countBySetorAndDesligadoEmBetween(setor, inicio, fim);
        long ativosInicio = funcionarioRepository.contarAtivosNoMomento(setor, inicio);
        long ativosFim = funcionarioRepository.contarAtivosNoMomento(setor, fim);
        double mediaAtivos = (ativosInicio + ativosFim) / 2.0;

        if (mediaAtivos <= 0) {
            return 0.0;
        }
        return Math.round(((desligados / mediaAtivos) * 100.0) * 100.0) / 100.0;
    }
}
