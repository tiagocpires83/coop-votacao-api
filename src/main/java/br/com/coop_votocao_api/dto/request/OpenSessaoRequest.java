package br.com.coop_votocao_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request para abertura de sessão de votação")
public class OpenSessaoRequest {

    @Min(1)
    @Schema(description = "Duração da sessão em minutos (default = 1)", example = "5", nullable = true)
    private Integer duracaoEmMinutos;

}
