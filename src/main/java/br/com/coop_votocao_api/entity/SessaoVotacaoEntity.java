package br.com.coop_votocao_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(
        name = "SESSAO_VOTACAO",
        uniqueConstraints = @UniqueConstraint(name = "UQ_SESSAO_PAUTA", columnNames = "PAUTA_ID")
)
public class SessaoVotacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SESSAO_GEN")
    @SequenceGenerator(name = "SEQ_SESSAO_GEN", sequenceName = "SEQ_SESSAO", allocationSize = 1)
    @Column(name = "ID", nullable = false, precision = 19, scale = 0)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PAUTA_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_SESSAO_PAUTA"))
    private PautaEntity pauta;

    @Column(name = "INICIO", nullable = false)
    private OffsetDateTime inicio;

    @Column(name = "FIM", nullable = false)
    private OffsetDateTime fim;

    public Long getPautaId() {
        return pauta != null ? pauta.getId() : null;
    }
}
