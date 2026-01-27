package br.com.coop_votocao_api.service;

import br.com.coop_votocao_api.client.CpfValidationClient;
import br.com.coop_votocao_api.dto.request.CreatePautaRequest;
import br.com.coop_votocao_api.dto.request.CreateVotoRequest;
import br.com.coop_votocao_api.dto.request.OpenSessaoRequest;
import br.com.coop_votocao_api.dto.response.PautaResponse;
import br.com.coop_votocao_api.dto.response.ResultadoResponse;
import br.com.coop_votocao_api.dto.response.SessaoResponse;
import br.com.coop_votocao_api.dto.response.VotoResponse;
import br.com.coop_votocao_api.entity.PautaEntity;
import br.com.coop_votocao_api.entity.SessaoVotacaoEntity;
import br.com.coop_votocao_api.entity.VotoEntity;
import br.com.coop_votocao_api.repository.PautaRepository;
import br.com.coop_votocao_api.repository.SessaoVotacaoRepository;
import br.com.coop_votocao_api.repository.VotoRepository;
import br.com.coop_votocao_api.rules.PautaRules;
import br.com.coop_votocao_api.rules.SessaoRules;
import br.com.coop_votocao_api.rules.VotoRules;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class VotacaoService {

    private final PautaRepository pautaRepository;
    private final SessaoVotacaoRepository sessaoRepository;
    private final VotoRepository votoRepository;

    private final PautaRules pautaRules;
    private final SessaoRules sessaoRules;
    private final VotoRules votoRules;

    private final CpfValidationClient cpfClient;
    private final Clock clock;

    private OffsetDateTime nowUtc() {
        return OffsetDateTime.now(clock.withZone(ZoneOffset.UTC));
    }

    @Transactional
    public PautaResponse criarPauta(CreatePautaRequest req) {
        OffsetDateTime now = nowUtc();

        log.info("Criando pauta titulo='{}'", req.titulo());

        PautaEntity pauta = PautaEntity.builder()
                .titulo(req.titulo())
                .descricao(req.descricao())
                .createdAt(now)
                .build();

        PautaEntity saved = pautaRepository.save(pauta);

        log.info("Pauta criada id={}", saved.getId());

        return new PautaResponse(saved.getId(), saved.getTitulo(), saved.getDescricao(), saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public PautaResponse buscarPauta(Long pautaId) {
        log.info("Buscando pauta id={}", pautaId);

        PautaEntity pauta = pautaRules.getPautaOrThrow(pautaId);

        return new PautaResponse(pauta.getId(), pauta.getTitulo(), pauta.getDescricao(), pauta.getCreatedAt());
    }

    @Transactional
    public SessaoResponse abrirSessao(Long pautaId, OpenSessaoRequest req) {
        log.info("Abrindo sessão pautaId={}", pautaId);

        PautaEntity pauta = pautaRules.getPautaOrThrow(pautaId);
        sessaoRules.assertSessaoNaoExiste(pautaId);

        OffsetDateTime inicio = nowUtc();
        OffsetDateTime fim = sessaoRules.calcularFim(inicio, req);

        SessaoVotacaoEntity sessao = SessaoVotacaoEntity.builder()
                .pauta(pauta)
                .inicio(inicio)
                .fim(fim)
                .build();

        sessaoRepository.save(sessao);

        log.info("Sessão aberta pautaId={} inicio={} fim={}", pautaId, inicio, fim);

        return new SessaoResponse(pautaId, inicio, fim);
    }

    @Transactional
    public VotoResponse votar(Long pautaId, CreateVotoRequest req) {
        log.info("Votando pautaId={} cpf={}", pautaId, req.cpf());

        PautaEntity pauta = pautaRules.getPautaOrThrow(pautaId);

        SessaoVotacaoEntity sessao = sessaoRules.getSessaoOrThrow(pautaId);

        OffsetDateTime now = nowUtc();
        sessaoRules.assertSessaoAberta(sessao, now);

        votoRules.assertNaoVotou(pautaId, req.cpf());

        cpfClient.validate(req.cpf());

        VotoEntity.VotoOpcao votoEnum = (req.voto() == CreateVotoRequest.VotoOpcao.SIM)
                ? VotoEntity.VotoOpcao.SIM
                : VotoEntity.VotoOpcao.NAO;

        VotoEntity voto = VotoEntity.builder()
                .pauta(pauta)
                .cpf(req.cpf())
                .voto(votoEnum)
                .createdAt(now)
                .build();

        VotoEntity saved = votoRepository.save(voto);

        log.info("Voto registrado id={} pautaId={} cpf={} voto={}",
                saved.getId(), pautaId, saved.getCpf(), saved.getVoto());

        return new VotoResponse(
                saved.getId(),
                saved.getPauta().getId(),
                saved.getCpf(),
                saved.getVoto().name(),
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public ResultadoResponse resultado(Long pautaId) {
        log.info("Resultado pautaId={}", pautaId);

        pautaRules.assertExists(pautaId);

        SessaoVotacaoEntity sessao = sessaoRules.getSessaoOrThrow(pautaId);

        OffsetDateTime now = nowUtc();
        boolean aberta = sessaoRules.isAberta(sessao, now);

        long sim = votoRepository.countByPauta_IdAndVoto(pautaId, VotoEntity.VotoOpcao.SIM);
        long nao = votoRepository.countByPauta_IdAndVoto(pautaId, VotoEntity.VotoOpcao.NAO);

        return new ResultadoResponse(
                pautaId,
                aberta,
                sessao.getInicio(),
                sessao.getFim(),
                sim,
                nao,
                sim + nao
        );
    }
}
