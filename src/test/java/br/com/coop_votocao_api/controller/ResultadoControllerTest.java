package br.com.coop_votocao_api.controller;

import br.com.coop_votocao_api.config.TestClockConfig;
import br.com.coop_votocao_api.dto.response.ResultadoResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ResultadoControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private VotacaoService service;

    @InjectMocks
    private ResultadoController controller;

    @BeforeEach
    void setup() {
        Clock clock = new TestClockConfig().clock();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(clock))
                .build();
    }

    @Test
    void resultado_deveRetornar200_comBody() throws Exception {
        when(service.resultado(1L))
                .thenReturn(new ResultadoResponse(
                        1L,
                        true,
                        OffsetDateTime.parse("2026-01-24T18:00:00Z"),
                        OffsetDateTime.parse("2026-01-24T18:01:00Z"),
                        2,
                        1,
                        3
                ));

        mockMvc.perform(get("/api/v1/pautas/{pautaId}/resultado", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pautaId").value(1))
                .andExpect(jsonPath("$.aberta").value(true))
                .andExpect(jsonPath("$.totalSim").value(2))
                .andExpect(jsonPath("$.totalNao").value(1))
                .andExpect(jsonPath("$.total").value(3));
    }
}