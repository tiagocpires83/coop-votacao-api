package br.com.coop_votocao_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.*;


@Builder
@Schema(description = "Request para abertura de sessão de votação")
public record OpenSessaoRequest (

    @Min(1)
    @Schema(description = "Duração da sessão em minutos (default = 1)", example = "5", nullable = true)
    Integer duracaoEmMinutos

){}
