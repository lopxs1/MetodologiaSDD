package br.org.amde.digital.repository;

import br.org.amde.digital.domain.entity.Funcionario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    Optional<Funcionario> findByUsuarioId(Long usuarioId);

    @Query("""
            SELECT f
            FROM Funcionario f
            JOIN FETCH f.usuario u
            WHERE u.id = :usuarioId
              AND f.excluidoEm IS NULL
              AND u.excluidoEm IS NULL
            """)
    Optional<Funcionario> findAtivoByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT f
            FROM Funcionario f
            JOIN FETCH f.usuario u
            WHERE f.id = :id
              AND f.excluidoEm IS NULL
              AND u.excluidoEm IS NULL
            """)
    Optional<Funcionario> findAtivoByIdForUpdate(@Param("id") Long id);
}
