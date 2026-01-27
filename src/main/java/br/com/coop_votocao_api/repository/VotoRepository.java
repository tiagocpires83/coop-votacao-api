package br.com.coop_votocao_api.repository;

import br.com.coop_votocao_api.entity.VotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotoRepository extends JpaRepository<VotoEntity, Long> {

    boolean existsByPauta_IdAndCpf(Long pautaId, String cpf);
    long countByPauta_IdAndVoto(Long pautaId, VotoEntity.VotoOpcao voto);
}
