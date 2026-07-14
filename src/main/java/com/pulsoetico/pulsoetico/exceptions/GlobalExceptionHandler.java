package com.pulsoetico.pulsoetico.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErroResposta> tratarCredenciaisInvalidas(
            BadCredentialsException ex) {
        ErroResposta corpo = ErroResposta.de(
                HttpStatus.UNAUTHORIZED.value(),
                "Não autorizado",
                ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(corpo);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErroResposta> tratarNaoEncontrado(
            EntityNotFoundException ex) {
        ErroResposta corpo = ErroResposta.de(
                HttpStatus.NOT_FOUND.value(),
                "Não encontrado",
                ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(corpo);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResposta> tratarArgumentoInvalido(
            IllegalArgumentException ex) {
        ErroResposta corpo = ErroResposta.de(
                HttpStatus.CONFLICT.value(),
                "Conflito",
                ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(corpo);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(
            MethodArgumentNotValidException ex) {
        Map<String, String> campos = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(erro -> campos.put(
                        erro.getField(),
                        erro.getDefaultMessage()));

        ErroResposta corpo = ErroResposta.deValidacao(
                HttpStatus.BAD_REQUEST.value(),
                "Validação falhou",
                campos);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(corpo);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarErroGeral(
            Exception ex) {
        ErroResposta corpo = ErroResposta.de(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno",
                "Ocorreu um erro inesperado");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(corpo);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErroResposta> tratarServicoIndisponivel(
            IllegalStateException ex) {
        ErroResposta corpo = ErroResposta.de(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Serviço indisponível",
                ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(corpo);
    }
}