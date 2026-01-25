package br.com.coop_votocao_api.exception;

public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String message) { super(message); }
}
