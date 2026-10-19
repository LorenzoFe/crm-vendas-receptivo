package com.ibrowse.crm.exception;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ProblemDetail> tratarRegra(ResponseStatusException exception) {
		return resposta(HttpStatus.valueOf(exception.getStatusCode().value()), exception.getReason());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ProblemDetail> tratarValidacao(MethodArgumentNotValidException exception) {
		Map<String, String> campos = new TreeMap<>();
		exception.getBindingResult().getFieldErrors()
				.forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Confira os campos informados");
		problema.setProperty("campos", campos);
		return ResponseEntity.badRequest().body(problema);
	}

	@ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
			MissingServletRequestParameterException.class, HandlerMethodValidationException.class,
			ConstraintViolationException.class })
	public ResponseEntity<ProblemDetail> tratarFormato(Exception exception) {
		return resposta(HttpStatus.BAD_REQUEST, "JSON, parâmetro ou valor de campo inválido");
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ProblemDetail> tratarIntegridade(DataIntegrityViolationException exception) {
		return resposta(HttpStatus.CONFLICT, "A operação conflita com os dados existentes ou com registros vinculados");
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ProblemDetail> tratarAcessoNegado(AccessDeniedException exception) {
		Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
		if (autenticacao == null || autenticacao instanceof AnonymousAuthenticationToken) {
			return resposta(HttpStatus.UNAUTHORIZED, "Autenticação necessária");
		}
		return resposta(HttpStatus.FORBIDDEN, "Você não tem permissão para esta operação");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ProblemDetail> tratarInesperado(Exception exception) {
		if (exception instanceof ErrorResponse erro) {
			return ResponseEntity.status(erro.getStatusCode()).body(erro.getBody());
		}
		log.error("Erro inesperado", exception);
		return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente ou contate o suporte.");
	}

	private ResponseEntity<ProblemDetail> resposta(HttpStatus status, String detalhe) {
		return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(status, detalhe));
	}

}
