package br.org.amde.digital.repository;

import br.org.amde.digital.domain.entity.RegistroProntuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegistroProntuarioRepository extends JpaRepository<RegistroProntuario, Long> {

    @Query("""
            SELECT r
            FROM RegistroProntuario r
            JOIN FETCH r.categoria c
            LEFT JOIN FETCH r.registroSubstituido rs
            WHERE r.paciente.id = :pacienteId
              AND r.excluidoEm IS NULL
            ORDER BY r.criadoEm DESC, r.id DESC
            """)
    List<RegistroProntuario> findAtivosByPacienteIdComCategoria(@Param("pacienteId") Long pacienteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r
            FROM RegistroProntuario r
            WHERE r.id = :id
              AND r.excluidoEm IS NULL
            """)
    Optional<RegistroProntuario> findAtivoByIdForUpdate(@Param("id") Long id);

    @Query("""
            SELECT r
            FROM RegistroProntuario r
            JOIN FETCH r.paciente p
            JOIN FETCH p.usuario u
            JOIN FETCH r.categoria c
            WHERE r.id = :id
              AND r.excluidoEm IS NULL
            """)
    Optional<RegistroProntuario> findAtivoByIdComPacienteECategoria(@Param("id") Long id);

    @Query("""
            SELECT COUNT(r) > 0
            FROM RegistroProntuario r
            WHERE r.registroSubstituido.id = :documentoId
              AND r.excluidoEm IS NULL
            """)
    boolean existsSubstitutoAtivoPara(@Param("documentoId") Long documentoId);
}
