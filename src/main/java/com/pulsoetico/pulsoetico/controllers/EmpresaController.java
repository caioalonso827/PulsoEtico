package com.pulsoetico.pulsoetico.controllers;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulsoetico.pulsoetico.models.Permissoes;
import com.pulsoetico.pulsoetico.models.dtos.EmpresaDtos.AtualizarMembroRequest;
import com.pulsoetico.pulsoetico.models.dtos.EmpresaDtos.AtualizarSetorMembroRequest;
import com.pulsoetico.pulsoetico.models.dtos.EmpresaDtos.CargoRequest;
import com.pulsoetico.pulsoetico.models.dtos.EmpresaDtos.CargoResponse;
import com.pulsoetico.pulsoetico.models.dtos.EmpresaDtos.CodigoConviteResponse;
import com.pulsoetico.pulsoetico.models.dtos.EmpresaDtos.CriarEmpresaRequest;
import com.pulsoetico.pulsoetico.models.dtos.EmpresaDtos.EmpresaResponse;
import com.pulsoetico.pulsoetico.models.dtos.EmpresaDtos.EntrarPorCodigoRequest;
import com.pulsoetico.pulsoetico.models.dtos.EmpresaDtos.MembroResponse;
import com.pulsoetico.pulsoetico.models.dtos.EmpresaDtos.SetorEmpresaResponse;
import com.pulsoetico.pulsoetico.models.dtos.EmpresaDtos.VinculoEmpresaResponse;
import com.pulsoetico.pulsoetico.models.dtos.SetorRequest;
import com.pulsoetico.pulsoetico.security.FuncionarioUserDetails;
import com.pulsoetico.pulsoetico.services.EmpresaService;


import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @PostMapping
    public ResponseEntity<VinculoEmpresaResponse> criar(
            @Valid @RequestBody CriarEmpresaRequest request,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        empresaService.criar(
                                request,
                                principal.getFuncionario()
                        )
                );
    }

    @GetMapping("/minhas")
    public List<EmpresaResponse> listarMinhas(
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return empresaService.listarMinhas(
                principal.getFuncionario()
        );
    }

    @GetMapping("/{empresaId}")
    public EmpresaResponse buscar(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return empresaService.buscar(
                empresaId,
                principal.getFuncionario()
        );
    }

    @PostMapping("/entrar")
    public VinculoEmpresaResponse entrar(
            @Valid @RequestBody EntrarPorCodigoRequest request,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return empresaService.entrarPorCodigo(
                request,
                principal.getFuncionario()
        );
    }

    /**
     * Correção de bug: antes descartava o token novo que o service gera
     * (a pessoa perde a empresa ativa, então o token muda) e devolvia só
     * 204 sem corpo. Agora devolve 200 com o token atualizado, senão o
     * front fica preso usando um token que já não reflete mais a saída.
     */
    @DeleteMapping("/{empresaId}/sair")
    public VinculoEmpresaResponse sair(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return empresaService.sair(
                empresaId,
                principal.getFuncionario()
        );
    }

    /** Troca a empresa ativa pra outra que a pessoa já participa (sem precisar logar de novo). */
    @PostMapping("/{empresaId}/selecionar")
    public VinculoEmpresaResponse selecionar(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return empresaService.selecionar(
                empresaId,
                principal.getFuncionario()
        );
    }

    @PostMapping("/{empresaId}/codigo-convite")
    public CodigoConviteResponse gerarCodigo(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return empresaService.gerarCodigo(
                empresaId,
                principal.getFuncionario()
        );
    }

    @DeleteMapping("/{empresaId}/codigo-convite")
    public ResponseEntity<Void> desativarCodigo(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        empresaService.desativarCodigo(
                empresaId,
                principal.getFuncionario()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{empresaId}/cargos/permissoes")
    public List<Permissoes> permissoesDisponiveis() {
        return Arrays.asList(Permissoes.values());
    }

    @GetMapping("/{empresaId}/cargos")
    public List<CargoResponse> listarCargos(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return empresaService.listarCargos(
                empresaId,
                principal.getFuncionario()
        );
    }

    @PostMapping("/{empresaId}/cargos")
    public ResponseEntity<CargoResponse> criarCargo(
            @PathVariable Long empresaId,
            @Valid @RequestBody CargoRequest request,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        empresaService.criarCargo(
                                empresaId,
                                request,
                                principal.getFuncionario()
                        )
                );
    }

    @PutMapping("/{empresaId}/cargos/{cargoId}")
    public CargoResponse atualizarCargo(
            @PathVariable Long empresaId,
            @PathVariable Long cargoId,
            @Valid @RequestBody CargoRequest request,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return empresaService.atualizarCargo(
                empresaId,
                cargoId,
                request,
                principal.getFuncionario()
        );
    }

    @PatchMapping("/{empresaId}/membros/{membroId}/setor")
public MembroResponse atualizarSetorMembro(
        @PathVariable Long empresaId,
        @PathVariable Long membroId,
        @Valid
        @RequestBody
        AtualizarSetorMembroRequest request,
        @AuthenticationPrincipal
        FuncionarioUserDetails principal
) {
    return empresaService.atualizarSetorMembro(
            empresaId,
            membroId,
            request,
            principal.getFuncionario()
    );
}

    @DeleteMapping("/{empresaId}/cargos/{cargoId}")
    public ResponseEntity<Void> excluirCargo(
            @PathVariable Long empresaId,
            @PathVariable Long cargoId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        empresaService.excluirCargo(
                empresaId,
                cargoId,
                principal.getFuncionario()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{empresaId}/membros")
    public List<MembroResponse> listarMembros(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return empresaService.listarMembros(
                empresaId,
                principal.getFuncionario()
        );
    }

    @PatchMapping("/{empresaId}/membros/{membroId}")
    public MembroResponse atualizarMembro(
            @PathVariable Long empresaId,
            @PathVariable Long membroId,
            @Valid @RequestBody AtualizarMembroRequest request,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return empresaService.atualizarMembro(
                empresaId,
                membroId,
                request,
                principal.getFuncionario()
        );
    }

    @DeleteMapping("/{empresaId}/membros/{membroId}")
    public ResponseEntity<Void> removerMembro(
            @PathVariable Long empresaId,
            @PathVariable Long membroId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        empresaService.removerMembro(
                empresaId,
                membroId,
                principal.getFuncionario()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{empresaId}/setores")
    public List<SetorEmpresaResponse> listarSetores(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return empresaService.listarSetores(
                empresaId,
                principal.getFuncionario()
        );
    }

    @PostMapping("/{empresaId}/setores")
    public ResponseEntity<SetorEmpresaResponse> criarSetor(
            @PathVariable Long empresaId,
            @Valid @RequestBody SetorRequest request,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        empresaService.criarSetor(
                                empresaId,
                                request,
                                principal.getFuncionario()
                        )
                );
    }

    @PutMapping("/{empresaId}/setores/{setorId}")
    public SetorEmpresaResponse atualizarSetor(
            @PathVariable Long empresaId,
            @PathVariable Long setorId,
            @Valid @RequestBody SetorRequest request,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return empresaService.atualizarSetor(
                empresaId,
                setorId,
                request,
                principal.getFuncionario()
        );
    }

    @DeleteMapping("/{empresaId}/setores/{setorId}")
    public ResponseEntity<Void> excluirSetor(
            @PathVariable Long empresaId,
            @PathVariable Long setorId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        empresaService.excluirSetor(
                empresaId,
                setorId,
                principal.getFuncionario()
        );

        return ResponseEntity.noContent().build();
    }
}