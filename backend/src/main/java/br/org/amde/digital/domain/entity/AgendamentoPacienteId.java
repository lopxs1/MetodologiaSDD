package br.org.amde.digital.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AgendamentoPacienteId implements Serializable {

    @Column(name = "agendamento_id", nullable = false)
    private Long agendamentoId;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;
}
