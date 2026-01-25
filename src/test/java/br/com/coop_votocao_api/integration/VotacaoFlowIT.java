package br.com.coop_votocao_api.integration;

import br.com.coop_votocao_api.dto.request.CreatePautaRequest;
import br.com.coop_votocao_api.dto.request.CreateVotoRequest;
import br.com.coop_votocao_api.dto.request.OpenSessaoRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class VotacaoFlowIT {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    @Test
    void fluxo_completo_cria_pauta_abre_sessao_vota_resultado() throws Exception {
        var pautaReq = CreatePautaRequest.builder()
                .titulo("Pauta 1")
                .descricao("Descrição da pauta 1")
                .build();

        var createPautaBody = mvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(pautaReq)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.titulo").value("Pauta 1"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode pautaJson = mapper.readTree(createPautaBody);
        long pautaId = pautaJson.get("id").asLong();

        var sessaoReq = OpenSessaoRequest.builder()
                .duracaoEmMinutos(5)
                .build();

        mvc.perform(post("/api/v1/pautas/{pautaId}/sessao", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sessaoReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pautaId").value(pautaId))
                .andExpect(jsonPath("$.inicio").exists())
                .andExpect(jsonPath("$.fim").exists());

        var votoReq = CreateVotoRequest.builder()
                .associadoId(10L)
                .cpf("12345678901")
                .voto(CreateVotoRequest.VotoOpcao.SIM)
                .build();

        mvc.perform(post("/api/v1/pautas/{pautaId}/votos", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(votoReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.pautaId").value(pautaId))
                .andExpect(jsonPath("$.associadoId").value(10))
                .andExpect(jsonPath("$.voto").value("SIM"));

        mvc.perform(get("/api/v1/pautas/{pautaId}/resultado", pautaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pautaId").value(pautaId))
                .andExpect(jsonPath("$.totalSim").value(1))
                .andExpect(jsonPath("$.totalNao").value(0))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.aberta").value(true));
    }

    @Test
    void nao_deve_permitir_voto_duplicado_para_mesmo_associado_na_mesma_pauta() throws Exception {
        var pautaReq = CreatePautaRequest.builder()
                .titulo("Pauta Duplicada")
                .descricao("Desc")
                .build();

        var createPautaBody = mvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(pautaReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long pautaId = mapper.readTree(createPautaBody).get("id").asLong();

        mvc.perform(post("/api/v1/pautas/{pautaId}/sessao", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(OpenSessaoRequest.builder().duracaoEmMinutos(5).build())))
                .andExpect(status().isCreated());

        var votoReq = CreateVotoRequest.builder()
                .associadoId(10L)
                .cpf("12345678901")
                .voto(CreateVotoRequest.VotoOpcao.SIM)
                .build();

        mvc.perform(post("/api/v1/pautas/{pautaId}/votos", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(votoReq)))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/v1/pautas/{pautaId}/votos", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(votoReq)))
                .andExpect(status().isConflict());
    }
}
