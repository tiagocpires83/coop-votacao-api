package br.com.coop_votocao_api.controller;

import br.com.coop_votocao_api.dto.response.ResultadoResponse;
import br.com.coop_votocao_api.exception.ApiError;
import br.com.coop_votocao_api.service.VotacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Resultados", description = "Endpoints para consulta do resultado de votação")
@RestController
@RequestMapping("/api/v1/pautas")
@RequiredArgsConstructor
public class ResultadoController {

    private final VotacaoService service;

    @Operation(summary = "Obter resultado da votação",
            description = "Retorna o resultado consolidado da pauta, incluindo totais de SIM/NAO e se a sessão está aberta.")
    @ApiResponse(responseCode = "200", description = "Resultado retornado",
            content = @Content(schema = @Schema(implementation = ResultadoResponse.class)))
    @ApiResponse(responseCode = "404", description = "Pauta não encontrada",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{pautaId}/resultado")
    public ResponseEntity<ResultadoResponse> resultado(@PathVariable Long pautaId) {
        return ResponseEntity.ok(service.resultado(pautaId));
    }
}