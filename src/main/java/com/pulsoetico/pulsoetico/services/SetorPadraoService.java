package com.pulsoetico.pulsoetico.services;

import java.text.Normalizer;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.Empresa;
import com.pulsoetico.pulsoetico.models.Setor;
import com.pulsoetico.pulsoetico.repositories.SetorRepository;

@Service
public class SetorPadraoService {

    public static final String NOME_SETOR_PADRAO = "Não alocado";
    private static final String NOME_SETOR_PADRAO_NORMALIZADO =
            "nao alocado";

    private final SetorRepository setorRepository;

    public SetorPadraoService(SetorRepository setorRepository) {
        this.setorRepository = setorRepository;
    }

    @Transactional
    public Setor obterOuCriar(Empresa empresa) {
        return setorRepository
                .findByEmpresaIdAndNomeIgnoreCase(
                        empresa.getId(),
                        NOME_SETOR_PADRAO
                )
                .orElseGet(() -> buscarNomeEquivalenteOuCriar(empresa));
    }

    public boolean ehSetorPadrao(Setor setor) {
        return setor != null && ehNomePadrao(setor.getNome());
    }

    public boolean ehNomePadrao(String nome) {
        return NOME_SETOR_PADRAO_NORMALIZADO.equals(
                normalizarNome(nome)
        );
    }

    private Setor buscarNomeEquivalenteOuCriar(Empresa empresa) {
        return setorRepository
                .findAllByEmpresaIdOrderByNomeAsc(empresa.getId())
                .stream()
                .filter(this::ehSetorPadrao)
                .findFirst()
                .map(setor -> padronizarNome(setor))
                .orElseGet(() -> criar(empresa));
    }

    private Setor criar(Empresa empresa) {
        return setorRepository.save(
                Setor.builder()
                        .empresa(empresa)
                        .nome(NOME_SETOR_PADRAO)
                        .quantidadeColaboradores(0)
                        .build()
        );
    }

    private Setor padronizarNome(Setor setor) {
        if (NOME_SETOR_PADRAO.equals(setor.getNome())) {
            return setor;
        }

        setor.setNome(NOME_SETOR_PADRAO);
        return setorRepository.save(setor);
    }

    private String normalizarNome(String nome) {
        if (nome == null) {
            return "";
        }

        String semAcentos = Normalizer
                .normalize(nome.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return semAcentos
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }
}
