package br.com.coop_votocao_api.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    private OffsetDateTime nowUtc() {
        return OffsetDateTime.now(clock.withZone(ZoneOffset.UTC));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI(), ex);
    }

    @ExceptionHandler(CpfInvalidoException.class)
    public ResponseEntity<ApiError> handleCpfInvalido(CpfInvalidoException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI(), ex);
    }

    @ExceptionHandler(CpfInaptoException.class)
    public ResponseEntity<ApiError> handleCpfInapto(CpfInaptoException ex, HttpServletRequest req) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req.getRequestURI(), ex);
    }

    @ExceptionHandler({
            SessaoJaAbertaException.class,
            SessaoEncerradaException.class,
            VotoDuplicadoException.class
    })
    public ResponseEntity<ApiError> handleConflict(BusinessException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI(), ex);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiError> handleExternal(ExternalServiceException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), req.getRequestURI(), ex);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiError> handleValidation(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Dados inválidos", req.getRequestURI(), ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Erro inesperado em {}", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado", req.getRequestURI(), ex);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, String path, Exception ex) {
        ApiError body = new ApiError(
                nowUtc(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                null
        );
        return ResponseEntity.status(status).body(body);
    }
}
