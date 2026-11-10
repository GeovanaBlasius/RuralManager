package br.com.ruralmanager.comum;

import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Converte excecoes em respostas padronizadas (RFC 7807). Erros inesperados
 * viram uma mensagem generica: o detalhe vai para o log, nunca para o cliente.
 */
@RestControllerAdvice
public class TratadorDeErros {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ProblemDetail naoEncontrado(RecursoNaoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, "Nao encontrado", e.getMessage());
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    ProblemDetail regra(RegraDeNegocioException e) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negocio", e.getMessage());
    }

    @ExceptionHandler(AcessoProibidoException.class)
    ProblemDetail proibido(AcessoProibidoException e) {
        return problema(HttpStatus.FORBIDDEN, "Acesso negado", e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    ProblemDetail credenciais(BadCredentialsException e) {
        return problema(HttpStatus.UNAUTHORIZED, "Nao autorizado", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validacao(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
        ProblemDetail p = problema(HttpStatus.BAD_REQUEST, "Dados invalidos", "Corrija os campos indicados.");
        p.setProperty("campos", campos);
        return p;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail restricao(ConstraintViolationException e) {
        return problema(HttpStatus.BAD_REQUEST, "Dados invalidos", "Parametros invalidos.");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ProblemDetail corpoIlegivel(Exception e) {
        return problema(HttpStatus.BAD_REQUEST, "Requisicao invalida", "Nao foi possivel interpretar a requisicao.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail integridade(DataIntegrityViolationException e) {
        // O detalhe (nome de constraint, SQL) fica no log; o cliente recebe so o essencial.
        log.warn("Violacao de integridade: {}", e.getMostSpecificCause().getMessage());
        return problema(HttpStatus.CONFLICT, "Conflito",
                "A operacao conflita com dados existentes (registro duplicado ou ainda em uso).");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail inesperado(Exception e) {
        log.error("Erro inesperado", e);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente em instantes.");
    }

    private static ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detalhe);
        p.setTitle(titulo);
        p.setType(URI.create("about:blank"));
        return p;
    }
}
