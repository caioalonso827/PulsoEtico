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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(
        "/api/app/empresas/{empresaId}/formularios"
)
@Tag(
        name = "Formulários - Aplicativo",
        description = "Consulta e envio de formulários disponíveis para o funcionário"
)
public class FormularioAppController {

    private final FormularioService formularioService;

    public FormularioAppController(
            FormularioService formularioService
    ) {
        this.formularioService = formularioService;
    }

    @GetMapping("/disponiveis")
    @Operation(
            summary = "Listar formulários disponíveis",
            description = "Retorna todos os formulários disponíveis para o setor do funcionário. "
                    + "Cada formulário já contém todas as perguntas e as alternativas de 1 a 5."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Formulários retornados com todas as perguntas e alternativas"
    )
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

    @GetMapping("/aplicacoes/{aplicacaoId}")
    @Operation(
            summary = "Buscar um formulário completo",
            description = "Retorna uma aplicação específica com todas as perguntas e alternativas. "
                    + "Use o ID de cada pergunta no endpoint de respostas."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Formulário completo encontrado"
    )
    public AplicacaoFormularioResponse buscarDisponivel(
            @PathVariable Long empresaId,
            @PathVariable Long aplicacaoId,
            @AuthenticationPrincipal
            FuncionarioUserDetails principal
    ) {
        return formularioService.buscarDisponivel(
                empresaId,
                aplicacaoId,
                principal.getFuncionario()
        );
    }

    @PostMapping(
            "/aplicacoes/{aplicacaoId}/respostas"
    )
    @Operation(
            summary = "Responder formulário",
            description = "Envie uma resposta para cada pergunta retornada pelo GET. "
                    + "Todas as perguntas devem ser enviadas juntas no array respostas."
    )
    @ApiResponse(
            responseCode = "204",
            description = "Formulário respondido com sucesso"
    )
    public ResponseEntity<Void> responderFormulario(
            @PathVariable Long empresaId,
            @PathVariable Long aplicacaoId,
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Lista completa de respostas. O perguntaId vem do GET do formulário.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ResponderFormularioRequest.class
                            ),
                            examples = @ExampleObject(
                                    name = "Todas as perguntas",
                                    value = """
                                            {
                                              "respostas": [
                                                { "perguntaId": 1, "valor": 3 },
                                                { "perguntaId": 2, "valor": 5 },
                                                { "perguntaId": 3, "valor": 2 },
                                                { "perguntaId": 4, "valor": 4 },
                                                { "perguntaId": 5, "valor": 1 },
                                                { "perguntaId": 6, "valor": 3 },
                                                { "perguntaId": 7, "valor": 2 },
                                                { "perguntaId": 8, "valor": 4 }
                                              ]
                                            }
                                            """
                            )
                    )
            )
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
