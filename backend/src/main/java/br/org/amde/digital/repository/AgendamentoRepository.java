package br.org.amde.digital.repository;

import br.org.amde.digital.domain.entity.Agendamento;
import br.org.amde.digital.domain.enums.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long>, JpaSpecificationExecutor<Agendamento> {

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

    default boolean existsConflitoHorario(
            Long funcionarioId,
            OffsetDateTime dataInicio,
            OffsetDateTime dataFim,
            Long idIgnorado
    ) {
        if (idIgnorado == null) {
            return existsConflitoHorarioNovo(funcionarioId, dataInicio, dataFim);
        }
        return existsConflitoHorarioIgnorandoId(funcionarioId, dataInicio, dataFim, idIgnorado);
    }

    @Query("""
            SELECT COUNT(a) > 0
            FROM Agendamento a
            WHERE a.funcionario.id = :funcionarioId
              AND a.status <> br.org.amde.digital.domain.enums.StatusAgendamento.CANCELADO
              AND a.dataInicio < :dataFim
              AND a.dataFim > :dataInicio
            """)
    boolean existsConflitoHorarioNovo(
            @Param("funcionarioId") Long funcionarioId,
            @Param("dataInicio") OffsetDateTime dataInicio,
            @Param("dataFim") OffsetDateTime dataFim
    );

    @Query("""
            SELECT COUNT(a) > 0
            FROM Agendamento a
            WHERE a.funcionario.id = :funcionarioId
              AND a.status <> br.org.amde.digital.domain.enums.StatusAgendamento.CANCELADO
              AND a.id <> :idIgnorado
              AND a.dataInicio < :dataFim
              AND a.dataFim > :dataInicio
            """)
    boolean existsConflitoHorarioIgnorandoId(
            @Param("funcionarioId") Long funcionarioId,
            @Param("dataInicio") OffsetDateTime dataInicio,
            @Param("dataFim") OffsetDateTime dataFim,
            @Param("idIgnorado") Long idIgnorado
    );

    @Query("""
            SELECT a
            FROM Agendamento a
            JOIN FETCH a.funcionario f
            JOIN FETCH f.usuario fu
            LEFT JOIN FETCH a.pacientes ap
            LEFT JOIN FETCH ap.paciente p
            LEFT JOIN FETCH p.usuario pu
            WHERE a.id = :id
            """)
    Optional<Agendamento> findByIdComDetalhes(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT a
            FROM Agendamento a
            JOIN FETCH a.funcionario f
            JOIN FETCH f.usuario fu
            LEFT JOIN FETCH a.pacientes ap
            LEFT JOIN FETCH ap.paciente p
            LEFT JOIN FETCH p.usuario pu
            WHERE a.id IN :ids
            """)
    List<Agendamento> findAllByIdInComDetalhes(@Param("ids") List<Long> ids);
}
