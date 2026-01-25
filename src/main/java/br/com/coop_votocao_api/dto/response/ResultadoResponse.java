package br.com.coop_votocao_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response de resultado da votação de uma pauta")
public class ResultadoResponse {

    @Schema(example = "1")
    private Long pautaId;

    @Schema(example = "true")
    private boolean aberta;

    private OffsetDateTime inicio;
    private OffsetDateTime fim;

    @Schema(example = "10")
    private long totalSim;

    @Schema(example = "3")
    private long totalNao;

    @Schema(example = "13")
    private long total;

    public static ResultadoResponse of(Long pautaId, boolean aberta, OffsetDateTime inicio, OffsetDateTime fim, long totalSim, long totalNao) {
        return ResultadoResponse.builder()
                .pautaId(pautaId)
                .aberta(aberta)
                .inicio(inicio)
                .fim(fim)
                .totalSim(totalSim)
                .totalNao(totalNao)
                .total(totalSim + totalNao)
                .build();
    }
}
