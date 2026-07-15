package com.pulsoetico.pulsoetico.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulsoetico.pulsoetico.models.dtos.AplicacaoFormularioResponse;
import com.pulsoetico.pulsoetico.models.dtos.ResponderFormularioRequest;
import com.pulsoetico.pulsoetico.security.FuncionarioUserDetails;
import com.pulsoetico.pulsoetico.services.FormularioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(
        "/api/app/empresas/{empresaId}/formularios"
)
public class FormularioAppController {

    private final FormularioService formularioService;

    public FormularioAppController(
            FormularioService formularioService
    ) {
        this.formularioService = formularioService;
    }

    @GetMapping("/disponiveis")
    public List<AplicacaoFormularioResponse> listarDisponiveis(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal
            FuncionarioUserDetails principal
    ) {
        return formularioService.listarDisponiveis(
                empresaId,
                principal.getFuncionario()
        );
    }

    @PostMapping(
            "/aplicacoes/{aplicacaoId}/respostas"
    )
    public ResponseEntity<Void> responderFormulario(
            @PathVariable Long empresaId,
            @PathVariable Long aplicacaoId,
            @Valid
            @RequestBody
            ResponderFormularioRequest request,
            @AuthenticationPrincipal
            FuncionarioUserDetails principal
    ) {
        formularioService.responderFormulario(
                empresaId,
                aplicacaoId,
                request,
                principal.getFuncionario()
        );

        return ResponseEntity.noContent().build();
    }
}