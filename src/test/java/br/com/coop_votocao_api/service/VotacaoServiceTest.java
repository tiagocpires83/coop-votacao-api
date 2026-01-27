package br.com.coop_votocao_api.service;

import br.com.coop_votocao_api.client.CpfValidationClient;
import br.com.coop_votocao_api.dto.request.CreatePautaRequest;
import br.com.coop_votocao_api.dto.request.CreateVotoRequest;
import br.com.coop_votocao_api.dto.request.OpenSessaoRequest;
import br.com.coop_votocao_api.entity.PautaEntity;
import br.com.coop_votocao_api.entity.SessaoVotacaoEntity;
import br.com.coop_votocao_api.entity.VotoEntity;
import br.com.coop_votocao_api.exception.*;
import br.com.coop_votocao_api.repository.PautaRepository;
import br.com.coop_votocao_api.repository.SessaoVotacaoRepository;
import br.com.coop_votocao_api.repository.VotoRepository;
import br.com.coop_votocao_api.rules.PautaRules;
import br.com.coop_votocao_api.rules.SessaoRules;
import br.com.coop_votocao_api.rules.VotoRules;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VotacaoServiceTest {

    @Mock private PautaRepository pautaRepository;
    @Mock private SessaoVotacaoRepository sessaoRepository;
    @Mock private VotoRepository votoRepository;

    @Mock private PautaRules pautaRules;
    @Mock private SessaoRules sessaoRules;
    @Mock private VotoRules votoRules;

    @Mock private CpfValidationClient cpfClient;

    private Clock fixedClock;
    private VotacaoService service;

    private static OffsetDateTime odt(String isoInstant) {
        return OffsetDateTime.ofInstant(Instant.parse(isoInstant), ZoneOffset.UTC);
    }

    @BeforeEach
    void setup() throws Exception {
        fixedClock = Clock.fixed(Instant.parse("2026-01-24T18:00:00Z"), ZoneOffset.UTC);
        service = new VotacaoService(
                pautaRepository,
                sessaoRepository,
                votoRepository,
                pautaRules,
                sessaoRules,
                votoRules,
                cpfClient,
                fixedClock
        );
    }

    @Test
    void criarPauta_ok() throws Exception {
        var req = new CreatePautaRequest("Pauta A", "Desc");

        when(pautaRepository.save(any(PautaEntity.class))).thenAnswer(inv -> {
            var e = inv.getArgument(0, PautaEntity.class);
            Field id = PautaEntity.class.getDeclaredField("id");
            id.setAccessible(true);
            id.set(e, 1L);
            return e;
        });

        var resp = service.criarPauta(req);

        assertEquals(1L, resp.id());
        assertEquals("Pauta A", resp.titulo());
        assertEquals(odt("2026-01-24T18:00:00Z"), resp.createdAt());
    }

    @Test
    void abrirSessao_default_1_minuto_quando_request_null() {
        var pauta = PautaEntity.builder()
                .id(1L)
                .titulo("Pauta A")
                .createdAt(odt("2026-01-24T17:00:00Z"))
                .build();

        when(pautaRules.getPautaOrThrow(1L)).thenReturn(pauta);
        doNothing().when(sessaoRules).assertSessaoNaoExiste(1L);
        when(sessaoRules.calcularFim(any(), isNull())).thenReturn(odt("2026-01-24T18:01:00Z"));

        var resp = service.abrirSessao(1L, null);

        assertEquals(1L, resp.pautaId());
        assertEquals(odt("2026-01-24T18:00:00Z"), resp.inicio());
        assertEquals(odt("2026-01-24T18:01:00Z"), resp.fim());
        verify(sessaoRepository).save(any(SessaoVotacaoEntity.class));
    }

    @Test
    void abrirSessao_falha_quando_pauta_nao_existe() {
        when(pautaRules.getPautaOrThrow(999L)).thenThrow(new NotFoundException("Pauta não encontrada"));
        assertThrows(NotFoundException.class, () -> service.abrirSessao(999L, new OpenSessaoRequest(1)));
    }

    @Test
    void abrirSessao_falha_quando_ja_existe_sessao() {
        var pauta = PautaEntity.builder().id(1L).titulo("Pauta A").createdAt(odt("2026-01-24T17:00:00Z")).build();

        when(pautaRules.getPautaOrThrow(1L)).thenReturn(pauta);
        doThrow(new SessaoJaAbertaException("Sessão já foi aberta para esta pauta"))
                .when(sessaoRules).assertSessaoNaoExiste(1L);

        assertThrows(SessaoJaAbertaException.class, () -> service.abrirSessao(1L, new OpenSessaoRequest(1)));
    }

    @Test
    void votar_ok() throws Exception {
        var pauta = PautaEntity.builder().id(1L).titulo("Pauta A").createdAt(odt("2026-01-24T17:00:00Z")).build();
        var sessao = SessaoVotacaoEntity.builder()
                .pauta(pauta)
                .inicio(odt("2026-01-24T17:59:00Z"))
                .fim(odt("2026-01-24T18:10:00Z"))
                .build();

        when(pautaRules.getPautaOrThrow(1L)).thenReturn(pauta);
        when(sessaoRules.getSessaoOrThrow(1L)).thenReturn(sessao);
        doNothing().when(sessaoRules).assertSessaoAberta(eq(sessao), any());
        doNothing().when(votoRules).assertNaoVotou(1L, "12345678901");

        when(cpfClient.validate("12345678901")).thenReturn(CpfValidationClient.VotingStatus.ABLE_TO_VOTE);

        when(votoRepository.save(any(VotoEntity.class))).thenAnswer(inv -> {
            var e = inv.getArgument(0, VotoEntity.class);
            Field id = VotoEntity.class.getDeclaredField("id");
            id.setAccessible(true);
            id.set(e, 100L);
            return e;
        });

        var req = new CreateVotoRequest("12345678901", CreateVotoRequest.VotoOpcao.SIM);

        var resp = service.votar(1L, req);

        assertEquals(100L, resp.id());
        assertEquals(1L, resp.pautaId());
        assertEquals("12345678901", resp.cpf());
        assertEquals("SIM", resp.voto());

        verify(cpfClient).validate("12345678901");
        verify(votoRepository).save(any(VotoEntity.class));
    }

    @Test
    void votar_falha_quando_sessao_encerrada() {
        var pauta = PautaEntity.builder().id(1L).titulo("Pauta A").build();
        var sessao = SessaoVotacaoEntity.builder()
                .pauta(pauta)
                .inicio(odt("2026-01-24T17:00:00Z"))
                .fim(odt("2026-01-24T17:10:00Z"))
                .build();

        when(pautaRules.getPautaOrThrow(1L)).thenReturn(pauta);
        when(sessaoRules.getSessaoOrThrow(1L)).thenReturn(sessao);

        doThrow(new SessaoEncerradaException("Sessão de votação encerrada"))
                .when(sessaoRules).assertSessaoAberta(eq(sessao), any());

        var req = new CreateVotoRequest("12345678901", CreateVotoRequest.VotoOpcao.SIM);

        assertThrows(SessaoEncerradaException.class, () -> service.votar(1L, req));
    }

    @Test
    void votar_falha_quando_voto_duplicado() {
        var pauta = PautaEntity.builder().id(1L).titulo("Pauta A").build();
        var sessao = SessaoVotacaoEntity.builder()
                .pauta(pauta)
                .inicio(odt("2026-01-24T17:59:00Z"))
                .fim(odt("2026-01-24T18:10:00Z"))
                .build();

        when(pautaRules.getPautaOrThrow(1L)).thenReturn(pauta);
        when(sessaoRules.getSessaoOrThrow(1L)).thenReturn(sessao);

        doNothing().when(sessaoRules).assertSessaoAberta(eq(sessao), any());
        doThrow(new VotoDuplicadoException("CPF já votou nesta pauta"))
                .when(votoRules).assertNaoVotou(1L, "12345678901");

        var req = new CreateVotoRequest("12345678901", CreateVotoRequest.VotoOpcao.SIM);

        assertThrows(VotoDuplicadoException.class, () -> service.votar(1L, req));
        verifyNoInteractions(cpfClient);
    }

    @Test
    void resultado_ok() {
        var pauta = PautaEntity.builder().id(1L).build();
        var sessao = SessaoVotacaoEntity.builder()
                .pauta(pauta)
                .inicio(odt("2026-01-24T17:59:00Z"))
                .fim(odt("2026-01-24T18:10:00Z"))
                .build();

        doNothing().when(pautaRules).assertExists(1L);
        when(sessaoRules.getSessaoOrThrow(1L)).thenReturn(sessao);
        when(sessaoRules.isAberta(eq(sessao), any())).thenReturn(true);

        when(votoRepository.countByPauta_IdAndVoto(1L, VotoEntity.VotoOpcao.SIM)).thenReturn(2L);
        when(votoRepository.countByPauta_IdAndVoto(1L, VotoEntity.VotoOpcao.NAO)).thenReturn(1L);

        var resp = service.resultado(1L);

        assertTrue(resp.aberta());
        assertEquals(2L, resp.totalSim());
        assertEquals(1L, resp.totalNao());
        assertEquals(3L, resp.total());
    }
}
