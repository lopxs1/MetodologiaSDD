package br.org.amde.digital.repository;

import br.org.amde.digital.domain.entity.AgendamentoPaciente;
import br.org.amde.digital.domain.entity.AgendamentoPacienteId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgendamentoPacienteRepository extends JpaRepository<AgendamentoPaciente, AgendamentoPacienteId> {

    List<AgendamentoPaciente> findByAgendamentoId(Long agendamentoId);

    void deleteByAgendamentoIdAndPacienteId(Long agendamentoId, Long pacienteId);
}

