package com.pulsoetico.pulsoetico.services;


import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.Setor;
import com.pulsoetico.pulsoetico.models.dtos.SetorRequest;
import com.pulsoetico.pulsoetico.repositories.SetorRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SetorService {

    private final SetorRepository setorRepository;

    public SetorService(SetorRepository setorRepository) {
        this.setorRepository = setorRepository;
    }

    @Transactional
    public Setor criar(SetorRequest request) {
        if (setorRepository.existsByNomeAndEmpresaIsNull(request.nome())) {
            throw new IllegalArgumentException("Já existe um setor com o nome: " + request.nome());
        }
        Setor setor = Setor.builder()
                .nome(request.nome())
                .quantidadeColaboradores(request.quantidadeColaboradores())
                .build();
        return setorRepository.save(setor);
    }

    public List<Setor> listarTodos() {
        return setorRepository.findAll();
    }

    public Setor buscarPorId(Long id) {
        return setorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Setor não encontrado: " + id));
    }

    @Transactional
    public Setor atualizarIndicadoresManuais(Long id, com.pulsoetico.pulsoetico.models.dtos.IndicadoresManuaisRequest request) {
        Setor setor = buscarPorId(id);
        setor.setTaxaRotatividadeMensal(request.taxaRotatividadeMensal());
        setor.setQuantidadeDenunciasAnonimasMensal(request.quantidadeDenunciasAnonimasMensal());
        return setorRepository.save(setor);
    }
}
