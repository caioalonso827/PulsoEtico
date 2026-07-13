package com.pulsoetico.pulsoetico.services;


import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.AvaliacaoRisco;
import com.pulsoetico.pulsoetico.models.Recomendacao;
import com.pulsoetico.pulsoetico.repositories.RecomendacaoRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Traduz uma AvaliacaoRisco em ações concretas para o gestor/RH.
 */
@Service
public class RecomendacaoService {

    private final RecomendacaoRepository recomendacaoRepository;

    public RecomendacaoService(RecomendacaoRepository recomendacaoRepository) {
        this.recomendacaoRepository = recomendacaoRepository;
    }

    public List<Recomendacao> gerarRecomendacoes(AvaliacaoRisco avaliacao) {
        List<Recomendacao> recomendacoes = new ArrayList<>();

        if (avaliacao.getMediaHorasExtras() != null && avaliacao.getMediaHorasExtras() >= 10) {
            recomendacoes.add(criar(avaliacao,
                    "Horas extras acima do saudável. Considere reduzir a carga e redistribuir tarefas.",
                    Recomendacao.TipoRecomendacao.REDUZIR_HORAS_EXTRAS));
        }

        if (avaliacao.getMediaSeveridadeHumor() != null && avaliacao.getMediaSeveridadeHumor() >= 3.5) {
            recomendacoes.add(criar(avaliacao,
                    "Humor médio da equipe em queda. Recomendamos uma conversa aberta com o time.",
                    Recomendacao.TipoRecomendacao.CONVERSA_COM_EQUIPE));
        }

        if (avaliacao.getTaxaRotatividade() != null && avaliacao.getTaxaRotatividade() >= 15) {
            recomendacoes.add(criar(avaliacao,
                    "Rotatividade elevada no setor. Avalie redistribuir tarefas e revisar a carga da equipe.",
                    Recomendacao.TipoRecomendacao.REDISTRIBUIR_TAREFAS));
        }

        if (avaliacao.getQuantidadeDenunciasAnonimas() != null && avaliacao.getQuantidadeDenunciasAnonimas() >= 2) {
            recomendacoes.add(criar(avaliacao,
                    "Aumento em denúncias anônimas. Recomendamos apoio psicológico e treinamento de liderança.",
                    Recomendacao.TipoRecomendacao.APOIO_PSICOLOGICO));
            recomendacoes.add(criar(avaliacao,
                    "Reforce o treinamento de liderança do setor para prevenir conflitos recorrentes.",
                    Recomendacao.TipoRecomendacao.TREINAMENTO_LIDERANCA));
        }

        if (recomendacoes.isEmpty() && avaliacao.getNivelRisco() != AvaliacaoRisco.NivelRisco.BAIXO) {
            recomendacoes.add(criar(avaliacao,
                    "Índice de risco em alta. Recomendamos acompanhar o setor de perto nas próximas semanas.",
                    Recomendacao.TipoRecomendacao.CONVERSA_COM_EQUIPE));
        }

        return recomendacaoRepository.saveAll(recomendacoes);
    }

    public List<Recomendacao> listarPendentesPorSetor(Long setorId) {
        return recomendacaoRepository.findByAvaliacaoRisco_Setor_IdAndReconhecidaFalse(setorId);
    }

    @Transactional
    public Recomendacao reconhecer(Long recomendacaoId) {
        Recomendacao recomendacao = recomendacaoRepository.findById(recomendacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Recomendação não encontrada: " + recomendacaoId));
        recomendacao.setReconhecida(true);
        return recomendacaoRepository.save(recomendacao);
    }

    private Recomendacao criar(AvaliacaoRisco avaliacao, String mensagem, Recomendacao.TipoRecomendacao tipo) {
        return Recomendacao.builder()
                .avaliacaoRisco(avaliacao)
                .mensagem(mensagem)
                .tipo(tipo)
                .build();
    }
}