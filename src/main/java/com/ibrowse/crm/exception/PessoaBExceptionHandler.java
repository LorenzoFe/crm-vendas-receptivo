package com.ibrowse.crm.exception;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import com.ibrowse.crm.controller.PessoaBController;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice(annotations = PessoaBController.class)
public class PessoaBExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> tratarRegra(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(
                ProblemDetail.forStatusAndDetail(exception.getStatusCode(), exception.getReason()));
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

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            HandlerMethodValidationException.class, ConstraintViolationException.class})
    public ResponseEntity<ProblemDetail> tratarFormato(Exception exception) {
        return ResponseEntity.badRequest().body(ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "JSON, parâmetro ou valor de campo inválido"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> tratarIntegridade(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "A operação conflita com os dados existentes ou com registros vinculados"));
    }
}
