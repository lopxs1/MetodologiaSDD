package br.org.amde.digital.repository;

import br.org.amde.digital.domain.entity.Agendamento;
import br.org.amde.digital.domain.enums.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    boolean existsByFuncionarioIdAndStatus(Long funcionarioId, StatusAgendamento status);

    @Query("""
            SELECT COUNT(ap) > 0
            FROM AgendamentoPaciente ap
            JOIN ap.agendamento a
            WHERE ap.paciente.id = :pacienteId
              AND a.status = :status
            """)
    boolean existsByPacienteIdAndStatus(
            @Param("pacienteId") Long pacienteId,
            @Param("status") StatusAgendamento status
    );
}
