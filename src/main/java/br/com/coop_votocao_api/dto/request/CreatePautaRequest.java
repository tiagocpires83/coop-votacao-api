package br.com.coop_votocao_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@Schema(description = "Request para criação de uma pauta")
public record CreatePautaRequest (

    @NotBlank
    @Size(max = 200)
    @Schema(description = "Título da pauta", example = "Pauta sobre investimento")
    String titulo,

    @Size(max = 1000)
    @Schema(example = "Pauta sobre investimento")
    String descricao
){}
