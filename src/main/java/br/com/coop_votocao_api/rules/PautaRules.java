package br.com.coop_votocao_api.rules;

import br.com.coop_votocao_api.entity.PautaEntity;
import br.com.coop_votocao_api.exception.NotFoundException;
import br.com.coop_votocao_api.repository.PautaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PautaRules {

    private final PautaRepository pautaRepository;

    public PautaEntity getPautaOrThrow(Long pautaId) {
        return pautaRepository.findById(pautaId)
                .orElseThrow(() -> new NotFoundException("Pauta não encontrada"));
    }

    public void assertExists(Long pautaId) {
        if (!pautaRepository.existsById(pautaId)) {
            throw new NotFoundException("Pauta não encontrada");
        }
    }
}
