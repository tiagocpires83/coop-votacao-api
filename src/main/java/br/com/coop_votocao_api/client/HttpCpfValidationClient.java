package br.com.coop_votocao_api.client;


import br.com.coop_votocao_api.exception.CpfInaptoException;
import br.com.coop_votocao_api.exception.CpfInvalidoException;
import br.com.coop_votocao_api.exception.ExternalServiceException;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;


@Component
@RequiredArgsConstructor
public class HttpCpfValidationClient implements CpfValidationClient {

    private final RestClient restClient;

    @Value("${cpf-validation.enabled:true}")
    private boolean enabled;

    @Override
    public CpfValidationClient.VotingStatus validate(String cpf) {
        if (!enabled) return CpfValidationClient.VotingStatus.ABLE_TO_VOTE;

        try {
            CpfStatusResponse resp = restClient.get()
                    .uri("/users/{cpf}", cpf)
                    .retrieve()
                    .body(CpfStatusResponse.class);

            if (resp == null || resp.status == null) {
                throw new ExternalServiceException("Resposta inválida do serviço de CPF");
            }

            if (resp.status == CpfValidationClient.VotingStatus.UNABLE_TO_VOTE) {
                throw new CpfInaptoException("CPF não está apto a votar");
            }

            return resp.status;

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new CpfInvalidoException("CPF inválido");
            }
            throw new ExternalServiceException("Falha ao consultar serviço de CPF: " + e.getStatusCode());
        } catch (ResourceAccessException e) {
            // normalmente timeout/conexão recusada
            throw new ExternalServiceException("Falha ao consultar serviço de CPF (timeout/conexão)");
        } catch (CpfInaptoException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalServiceException("Falha ao consultar serviço de CPF");
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    static class CpfStatusResponse {
        @JsonProperty("status")
        private CpfValidationClient.VotingStatus status;
    }
}
