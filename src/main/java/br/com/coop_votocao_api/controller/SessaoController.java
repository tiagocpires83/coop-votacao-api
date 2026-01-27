package br.com.coop_votocao_api.controller;

import br.com.coop_votocao_api.dto.request.OpenSessaoRequest;
import br.com.coop_votocao_api.dto.response.SessaoResponse;
import br.com.coop_votocao_api.exception.ApiError;
import br.com.coop_votocao_api.service.VotacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "Sessões", description = "Endpoints para abertura de sessão de votação")
@RestController
@RequestMapping("/api/v1/pautas")
@RequiredArgsConstructor
public class SessaoController {

    private final VotacaoService service;

    @Operation(summary = "Abrir sessão de votação",
            description = "Abre uma sessão de votação para uma pauta. Se o body for omitido/null, a duração padrão do sistema deve ser aplicada.")
    @ApiResponse(responseCode = "201", description = "Sessão aberta",
            content = @Content(schema = @Schema(implementation = SessaoResponse.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Pauta não encontrada",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Sessão já aberta para a pauta",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/{pautaId}/sessao")
    public ResponseEntity<SessaoResponse> abrirSessao(
            @PathVariable Long pautaId,
            @Valid @RequestBody(required = false) OpenSessaoRequest req
    ) {
        SessaoResponse resp = service.abrirSessao(pautaId, req);
        return ResponseEntity.created(URI.create("/api/v1/pautas/" + pautaId + "/sessao")).body(resp);
    }
}
