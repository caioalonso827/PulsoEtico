package com.pulsoetico.pulsoetico.services;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.Denuncia;
import com.pulsoetico.pulsoetico.models.Setor;
import com.pulsoetico.pulsoetico.models.dtos.DenunciaRequest;
import com.pulsoetico.pulsoetico.repositories.DenunciaRepository;
import com.pulsoetico.pulsoetico.repositories.SetorRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class DenunciaService {

    private final DenunciaRepository denunciaRepository;
    private final SetorRepository setorRepository;

    public DenunciaService(DenunciaRepository denunciaRepository, SetorRepository setorRepository) {
        this.denunciaRepository = denunciaRepository;
        this.setorRepository = setorRepository;
    }

    @Transactional
    public Denuncia registrarAnonimamente(DenunciaRequest request) {
        Setor setor = setorRepository.findById(request.setorId())
                .orElseThrow(() -> new EntityNotFoundException("Setor não encontrado: " + request.setorId()));

        Denuncia denuncia = Denuncia.builder()
                .setor(setor)
                .categoria(request.categoria().trim())
                .descricao(normalizarDescricao(request.descricao()))
                .build();

        return denunciaRepository.save(denuncia);
    }

    public int contarNoPeriodo(Setor setor, Instant inicio, Instant fim) {
        long quantidade = denunciaRepository.countBySetorAndCriadoEmBetween(setor, inicio, fim);
        return quantidade > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) quantidade;
    }

    private String normalizarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return null;
        }
        return descricao.trim();
    }
}
