package br.com.coop_votocao_api.rules;

import br.com.coop_votocao_api.exception.VotoDuplicadoException;
import br.com.coop_votocao_api.repository.VotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VotoRules {

    private final VotoRepository votoRepository;

    public void assertNaoVotou(Long pautaId, String cpf) {
        if (votoRepository.existsByPauta_IdAndCpf(pautaId, cpf)) {
            throw new VotoDuplicadoException("CPF já votou nesta pauta");
        }
    }
}
