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
        name = "VOTO",
        uniqueConstraints = @UniqueConstraint(
                name = "UQ_VOTO_PAUTA_ASSOCIADO",
                columnNames = {"PAUTA_ID", "ASSOCIADO_ID"}
        )
)
public class VotoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VOTO_GEN")
    @SequenceGenerator(name = "SEQ_VOTO_GEN", sequenceName = "SEQ_VOTO", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PAUTA_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_VOTO_PAUTA"))
    private PautaEntity pauta;

    @Column(name = "ASSOCIADO_ID", nullable = false)
    private Long associadoId;

    @Column(name = "CPF", nullable = false, length = 20)
    private String cpf;

    @Enumerated(EnumType.STRING)
    @Column(name = "VOTO", nullable = false, length = 10)
    private VotoOpcao voto;

    @Column(name = "CREATED_AT", nullable = false)
    private OffsetDateTime createdAt;

    public enum VotoOpcao { SIM, NAO }

    public Long getPautaId() {
        return pauta != null ? pauta.getId() : null;
    }
}
