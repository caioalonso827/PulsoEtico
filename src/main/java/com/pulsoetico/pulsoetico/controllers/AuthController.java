package com.pulsoetico.pulsoetico.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pulsoetico.pulsoetico.models.dtos.CadastroRequest;
import com.pulsoetico.pulsoetico.models.dtos.LoginPendenteResponse;
import com.pulsoetico.pulsoetico.models.dtos.LoginRequest;
import com.pulsoetico.pulsoetico.models.dtos.LoginResponse;
import com.pulsoetico.pulsoetico.models.dtos.VerificarCodigoRequest;
import com.pulsoetico.pulsoetico.security.FuncionarioUserDetails;
import com.pulsoetico.pulsoetico.services.AuthService;
import com.pulsoetico.pulsoetico.services.DispositivoConfiavelCookieService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final DispositivoConfiavelCookieService cookieService;

    public AuthController(
            AuthService authService,
            DispositivoConfiavelCookieService cookieService
    ) {
        this.authService = authService;
        this.cookieService = cookieService;
    }

    /**
     * O token do dispositivo pode vir de três lugares:
     * 1) LoginRequest.dispositivoToken, usado principalmente por apps;
     * 2) cabeçalho X-Dispositivo-Token;
     * 3) cookie HttpOnly gravado após a confirmação do código.
     *
     * Quando algum deles identifica um dispositivo válido e não expirado,
     * o login é concluído sem enviar outro código.
     */
    @PostMapping("/login")
    public ResponseEntity<Object> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(
                    value = DispositivoConfiavelCookieService.HEADER_DISPOSITIVO_TOKEN,
                    required = false
            )
            String tokenDoCabecalho,
            HttpServletRequest httpRequest
    ) {
        String dispositivoToken = cookieService.resolverToken(
                request.dispositivoToken(),
                tokenDoCabecalho,
                httpRequest.getCookies()
        );

        return ResponseEntity.ok(
                authService.login(
                        request,
                        dispositivoToken
                )
        );
    }

    /**
     * Por padrão o logout mantém a confiança do dispositivo. Assim, ao entrar
     * novamente no mesmo aparelho dentro de 90 dias, não será pedido código.
     * Use esquecerDispositivo=true para apagar o cookie local.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal FuncionarioUserDetails principal,
            @RequestParam(
                    name = "esquecerDispositivo",
                    defaultValue = "false"
            )
            boolean esquecerDispositivo,
            HttpServletResponse httpResponse
    ) {
        if (esquecerDispositivo) {
            cookieService.apagarCookie(httpResponse);
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Confirma tanto o código obrigatório do cadastro quanto o código de um
     * primeiro login. Depois da confirmação, o dispositivo fica confiável por
     * 90 dias e o token é entregue no JSON, no header e em cookie HttpOnly.
     */
    @PostMapping("/verificar-codigo")
    public ResponseEntity<LoginResponse> verificarCodigo(
            @Valid @RequestBody VerificarCodigoRequest request,
            @RequestHeader(
                    value = "User-Agent",
                    required = false
            )
            String userAgent,
            HttpServletResponse httpResponse
    ) {
        LoginResponse response = authService.verificarCodigo(
                request,
                userAgent
        );

        cookieService.gravarCookie(
                httpResponse,
                response.dispositivoToken()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * O cadastro sempre exige código. Um cookie ou token de dispositivo já
     * existente nunca pula esta etapa.
     */
    @PostMapping("/cadastro")
    public ResponseEntity<LoginPendenteResponse> cadastrar(
            @Valid @RequestBody CadastroRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.cadastrar(request));
    }
}
