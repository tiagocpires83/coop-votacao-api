package br.com.coop_votocao_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request para criação de uma pauta")
public class CreatePautaRequest {

    @NotBlank
    @Size(max = 200)
    @Schema(description = "Título da pauta", example = "Pauta sobre investimento")
    private String titulo;

    @Size(max = 1000)
    @Schema(description = "Descrição da pauta", example = "Discussão sobre investimento em nova iniciativa")
    private String descricao;
}
