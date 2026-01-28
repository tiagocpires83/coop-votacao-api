package br.com.coop_votocao_api.controller;


import br.com.coop_votocao_api.config.TestClockConfig;
import br.com.coop_votocao_api.dto.request.CreatePautaRequest;
import br.com.coop_votocao_api.dto.response.PautaResponse;
import br.com.coop_votocao_api.exception.GlobalExceptionHandler;
import br.com.coop_votocao_api.service.VotacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PautaControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private VotacaoService service;

    @InjectMocks
    private PautaController controller;

    @BeforeEach
    void setup() {

        Clock clock = new TestClockConfig().clock();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(clock))
                .build();
    }

    @Test
    void criarPauta_deveRetornar201_comLocationEBody() throws Exception {
        var request = new CreatePautaRequest("Pauta Teste", "Descrição");

        when(service.criarPauta(request))
                .thenReturn(new PautaResponse(
                        1L,
                        "Pauta Teste",
                        "Descrição",
                        OffsetDateTime.parse("2026-01-24T18:00:00Z")
                ));

        mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/pautas/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Pauta Teste"));
    }

    @Test
    void criarPauta_deveRetornar400_quandoTituloInvalido() throws Exception {
        var request = new CreatePautaRequest("", "Desc");

        mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
