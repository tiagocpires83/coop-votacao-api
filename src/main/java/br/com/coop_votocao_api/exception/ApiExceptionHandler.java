package br.com.coop_votocao_api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return base(req, 404, "NOT_FOUND", ex.getMessage(), null);
    }

    @ExceptionHandler(CpfInvalidoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleCpfInvalido(CpfInvalidoException ex, HttpServletRequest req) {
        return base(req, 404, "CPF_INVALIDO", ex.getMessage(), null);
    }

    @ExceptionHandler(CpfInaptoException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiError handleCpfInapto(CpfInaptoException ex, HttpServletRequest req) {
        return base(req, 422, "CPF_INAPTO", ex.getMessage(), null);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleBusiness(BusinessException ex, HttpServletRequest req) {
        return base(req, 409, "CONFLICT", ex.getMessage(), null);
    }

    @ExceptionHandler(ExternalServiceException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ApiError handleExternal(ExternalServiceException ex, HttpServletRequest req) {
        return base(req, 502, "BAD_GATEWAY", ex.getMessage(), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        return base(req, 409, "CONFLICT", "Violação de integridade (possível voto duplicado)", null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> details = new HashMap<>();
        for (var err : ex.getBindingResult().getAllErrors()) {
            if (err instanceof FieldError fe) {
                details.put(fe.getField(), fe.getDefaultMessage());
            }
        }
        return base(req, 400, "BAD_REQUEST", "Dados inválidos", details);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGeneric(Exception ex, HttpServletRequest req) {
        return base(req, 500, "INTERNAL_SERVER_ERROR", "Erro inesperado", null);
    }

    private ApiError base(HttpServletRequest req, int status, String error, String message, Map<String, String> details) {
        return ApiError.builder()
                .timestamp(Instant.now())
                .status(status)
                .error(error)
                .message(message)
                .path(req.getRequestURI())
                .details(details)
                .build();
    }
}
