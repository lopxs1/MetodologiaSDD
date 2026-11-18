package br.org.amde.digital.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.OffsetDateTime;

@Entity
@Table(name = "agendamento_paciente", schema = "amde_digital")
@Getter
@Setter
@NoArgsConstructor
public class AgendamentoPaciente {

    @EmbeddedId
    private AgendamentoPacienteId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("agendamentoId")
    @JoinColumn(name = "agendamento_id", nullable = false)
    private Agendamento agendamento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("pacienteId")
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Column(name = "possui_presenca", nullable = false)
    private Boolean possuiPresenca = false;

    @Generated(event = EventType.INSERT)
    @Column(name = "criado_em", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "atualizado_em", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime atualizadoEm;

    @Override
    public String toString() {
        return "AgendamentoPaciente{id=" + id + ", possuiPresenca=" + possuiPresenca + "}";
    }
}
