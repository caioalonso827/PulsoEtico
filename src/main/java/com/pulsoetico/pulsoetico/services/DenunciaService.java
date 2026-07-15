package com.pulsoetico.pulsoetico.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.Denuncia;
import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.MembroEmpresa;
import com.pulsoetico.pulsoetico.models.Permissoes;
import com.pulsoetico.pulsoetico.models.Setor;
import com.pulsoetico.pulsoetico.models.dtos.DenunciaRequest;
import com.pulsoetico.pulsoetico.repositories.DenunciaRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class DenunciaService {

    private static final int HORAS_LIMITE_RESPOSTA = 48;

    private final DenunciaRepository denunciaRepository;
    private final AutorizacaoEmpresaService autorizacao;

    public DenunciaService(
            DenunciaRepository denunciaRepository,
            AutorizacaoEmpresaService autorizacao
    ) {
        this.denunciaRepository = denunciaRepository;
        this.autorizacao = autorizacao;
    }

    @Transactional
    public Denuncia registrarAnonimamente(
            DenunciaRequest request,
            Funcionario funcionarioLogado,
            Long empresaId
    ) {
        MembroEmpresa membro = autorizacao.exigirPermissao(
                empresaId,
                funcionarioLogado,
                Permissoes.RESPONDER_DENUNCIAS
        );

        Setor setor = membro.getSetor();

        if (setor == null) {
            throw new EntityNotFoundException(
                    "Você ainda não possui setor nesta empresa"
            );
        }

        Denuncia denuncia = Denuncia.builder()
                .setor(setor)
                .tipo(request.tipo().trim())
                .descricao(normalizarDescricao(request.descricao()))
                .build();

        return denunciaRepository.save(denuncia);
    }

    @Transactional(readOnly = true)
    public List<Denuncia> listarRecentesDaEmpresa(
            Long empresaId,
            Funcionario usuario
    ) {
        autorizacao.exigirPermissao(
                empresaId,
                usuario,
                Permissoes.GERENCIAR_DENUNCIAS
        );

        return listarRecentesDaEmpresa(empresaId);
    }

    @Transactional(readOnly = true)
    public List<Denuncia> listarRecentesDaEmpresa(
            Long empresaId
    ) {
        return denunciaRepository
                .findTop20BySetor_Empresa_IdOrderByCriadoEmDesc(
                        empresaId
                );
    }

    @Transactional
    public Denuncia marcarComoRespondida(
            Long empresaId,
            Long denunciaId,
            Funcionario usuario
    ) {
        autorizacao.exigirPermissao(
                empresaId,
                usuario,
                Permissoes.GERENCIAR_DENUNCIAS
        );

        Denuncia denuncia = denunciaRepository
                .findByIdAndSetor_Empresa_Id(
                        denunciaId,
                        empresaId
                )
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Denúncia não encontrada nesta empresa"
                        )
                );

        denuncia.setStatus(
                Denuncia.StatusDenuncia.RESPONDIDA
        );
        denuncia.setRespondidaEm(Instant.now());

        return denunciaRepository.save(denuncia);
    }

    public int contarNoPeriodo(
            Setor setor,
            Instant inicio,
            Instant fim
    ) {
        long quantidade = denunciaRepository
                .countBySetorAndCriadoEmBetween(
                        setor,
                        inicio,
                        fim
                );

        return quantidade > Integer.MAX_VALUE
                ? Integer.MAX_VALUE
                : (int) quantidade;
    }

    public long contarAbertasDaEmpresa(Long empresaId) {
        return denunciaRepository
                .countBySetor_Empresa_IdAndStatus(
                        empresaId,
                        Denuncia.StatusDenuncia.ABERTA
                );
    }

    public long contarSemRespostaAlemDoLimiteDaEmpresa(
            Long empresaId
    ) {
        Instant limite = Instant.now().minus(
                HORAS_LIMITE_RESPOSTA,
                ChronoUnit.HOURS
        );

        return denunciaRepository
                .countBySetor_Empresa_IdAndStatusAndCriadoEmBefore(
                        empresaId,
                        Denuncia.StatusDenuncia.ABERTA,
                        limite
                );
    }

    private String normalizarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return null;
        }
        return descricao.trim();
    }
}
