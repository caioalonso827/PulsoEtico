package com.pulsoetico.pulsoetico.controllers;

import com.pulsoetico.pulsoetico.models.dtos.FuncionarioRequest;
import com.pulsoetico.pulsoetico.models.dtos.FuncionarioResponse;
import com.pulsoetico.pulsoetico.services.FuncionarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/painel/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @PostMapping
    public ResponseEntity<FuncionarioResponse> criar(@Valid @RequestBody FuncionarioRequest request) {
        var funcionario = funcionarioService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(FuncionarioResponse.from(funcionario));
    }

    @GetMapping
    public List<FuncionarioResponse> listarTodos() {
        return funcionarioService.listarTodos().stream()
                .map(FuncionarioResponse::from)
                .toList();
    }
}
