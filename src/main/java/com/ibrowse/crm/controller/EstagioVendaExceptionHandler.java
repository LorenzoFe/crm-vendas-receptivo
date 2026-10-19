package com.ibrowse.crm.controller;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(assignableTypes = EstagioVendaController.class)
public class EstagioVendaExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> tratarRegra(ResponseStatusException exception) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                exception.getStatusCode(), exception.getReason());
        return ResponseEntity.status(exception.getStatusCode()).body(problema);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> tratarValidacao(MethodArgumentNotValidException exception) {
        Map<String, String> campos = new TreeMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Confira os campos informados");
        problema.setProperty("campos", campos);
        return ResponseEntity.badRequest().body(problema);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ProblemDetail> tratarFormato(Exception exception) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "JSON ou parâmetro com formato inválido");
        return ResponseEntity.badRequest().body(problema);
    }
}
