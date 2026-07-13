package com.pulsoetico.pulsoetico.services;

import java.time.Instant;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.Denuncia;
import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.Setor;
import com.pulsoetico.pulsoetico.models.dtos.DenunciaRequest;
import com.pulsoetico.pulsoetico.repositories.DenunciaRepository;
import com.pulsoetico.pulsoetico.repositories.FuncionarioRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class DenunciaService {

    private final DenunciaRepository denunciaRepository;
    private final FuncionarioRepository funcionarioRepository;

    public DenunciaService(
            DenunciaRepository denunciaRepository,
            FuncionarioRepository funcionarioRepository) {

        this.denunciaRepository = denunciaRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    @Transactional
    public Denuncia registrarAnonimamente(
            DenunciaRequest request,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Usuário não autenticado");
        }

        String email = authentication.getName();

        Funcionario funcionario = funcionarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Funcionário não encontrado para o usuário autenticado"));

        Setor setor = funcionario.getSetor();

        if (setor == null) {
            throw new EntityNotFoundException(
                    "O funcionário autenticado não possui setor");
        }

        Denuncia denuncia = Denuncia.builder()
                .setor(setor)
                .tipo(request.tipo().trim())
                .descricao(normalizarDescricao(request.descricao()))
                .build();

        return denunciaRepository.save(denuncia);
    }

    public int contarNoPeriodo(Setor setor, Instant inicio, Instant fim) {
        long quantidade =
                denunciaRepository.countBySetorAndCriadoEmBetween(
                        setor,
                        inicio,
                        fim);

        return quantidade > Integer.MAX_VALUE
                ? Integer.MAX_VALUE
                : (int) quantidade;
    }

    private String normalizarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return null;
        }

        return descricao.trim();
    }

    @Transactional(readOnly = true)
    public List<Denuncia> listar() {
        return denunciaRepository.findAll();
    }
}