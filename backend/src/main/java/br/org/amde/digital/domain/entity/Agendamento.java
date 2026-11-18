package br.org.amde.digital.domain.entity;

import br.org.amde.digital.domain.enums.StatusAgendamento;
import br.org.amde.digital.domain.enums.TipoAgendamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "agendamento", schema = "amde_digital")
@Getter
@Setter
@NoArgsConstructor
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @Column(name = "data_inicio", nullable = false)
    private OffsetDateTime dataInicio;

    @Column(name = "data_fim", nullable = false)
    private OffsetDateTime dataFim;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_agendamento", nullable = false, columnDefinition = "text")
    private TipoAgendamento tipoAgendamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "text")
    private StatusAgendamento status = StatusAgendamento.AGENDADO;

    @Column(name = "possui_presenca_funcionario", nullable = false)
    private Boolean possuiPresencaFuncionario = false;

    @Generated(event = EventType.INSERT)
    @Column(name = "criado_em", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "atualizado_em", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime atualizadoEm;

    @OneToMany(mappedBy = "agendamento")
    private Set<AgendamentoPaciente> pacientes = new LinkedHashSet<>();

    @Override
    public String toString() {
        return "Agendamento{id=" + id + ", status=" + status + "}";
    }
}
