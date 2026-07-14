package com.pulsoetico.pulsoetico.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

    private static final int HORAS_LIMITE_RESPOSTA = 48;

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

    /** Últimas denúncias (qualquer setor), pro feed de alertas do gestor. */
    public List<Denuncia> listarRecentes() {
        return denunciaRepository.findTop20ByOrderByCriadoEmDesc();
    }

    public long contarAbertas() {
        return denunciaRepository.countByStatus(Denuncia.StatusDenuncia.ABERTA);
    }

    /** Abertas há mais de 48h sem resposta — o alerta "sem resposta há 48h". */
    public long contarSemRespostaAlemDoLimite() {
        Instant limite = Instant.now().minus(HORAS_LIMITE_RESPOSTA, ChronoUnit.HOURS);
        return denunciaRepository.countByStatusAndCriadoEmBefore(Denuncia.StatusDenuncia.ABERTA, limite);
    }

    @Transactional
    public Denuncia marcarComoRespondida(Long id) {
        Denuncia denuncia = denunciaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Denúncia não encontrada: " + id));
        denuncia.setStatus(Denuncia.StatusDenuncia.RESPONDIDA);
        denuncia.setRespondidaEm(Instant.now());
        return denunciaRepository.save(denuncia);
    }

    private String normalizarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return null;
        }

        return descricao.trim();
    }
}
