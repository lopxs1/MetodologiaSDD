package br.org.amde.digital.repository;

import br.org.amde.digital.domain.entity.Paciente;
import br.org.amde.digital.domain.enums.GrauSuporte;
import br.org.amde.digital.dto.paciente.PacienteResumoProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByUsuarioId(Long usuarioId);

    @Query("""
            SELECT p
            FROM Paciente p
            JOIN FETCH p.usuario u
            WHERE p.id = :id
              AND p.excluidoEm IS NULL
              AND u.excluidoEm IS NULL
            """)
    Optional<Paciente> findAtivoByIdComUsuario(@Param("id") Long id);

    @Query("""
            SELECT p
            FROM Paciente p
            JOIN FETCH p.usuario u
            WHERE u.id = :usuarioId
              AND p.excluidoEm IS NULL
              AND u.excluidoEm IS NULL
            """)
    Optional<Paciente> findAtivoByUsuarioIdComUsuario(@Param("usuarioId") Long usuarioId);

    default Page<PacienteResumoProjection> buscarAtivosPaginado(
            String padraoLike,
            GrauSuporte grauSuporte,
            Pageable pageable
    ) {
        if (padraoLike == null && grauSuporte == null) {
            return buscarTodosAtivos(pageable);
        }
        if (padraoLike == null) {
            return buscarAtivosPorGrau(grauSuporte, pageable);
        }
        if (grauSuporte == null) {
            return buscarAtivosPorTermo(padraoLike, pageable);
        }
        return buscarAtivosPorTermoEGrau(padraoLike, grauSuporte, pageable);
    }

    @Query(
            value = """
                    SELECT new br.org.amde.digital.dto.paciente.PacienteResumoProjection(
                        p.id, u.id, u.nome, u.email, p.grauSuporte, u.numCelular
                    )
                    FROM Paciente p
                    JOIN p.usuario u
                    WHERE p.excluidoEm IS NULL
                      AND u.excluidoEm IS NULL
                    """,
            countQuery = """
                    SELECT COUNT(p)
                    FROM Paciente p
                    JOIN p.usuario u
                    WHERE p.excluidoEm IS NULL
                      AND u.excluidoEm IS NULL
                    """
    )
    Page<PacienteResumoProjection> buscarTodosAtivos(Pageable pageable);

    @Query(
            value = """
                    SELECT new br.org.amde.digital.dto.paciente.PacienteResumoProjection(
                        p.id, u.id, u.nome, u.email, p.grauSuporte, u.numCelular
                    )
                    FROM Paciente p
                    JOIN p.usuario u
                    WHERE p.excluidoEm IS NULL
                      AND u.excluidoEm IS NULL
                      AND p.grauSuporte = :grauSuporte
                    """,
            countQuery = """
                    SELECT COUNT(p)
                    FROM Paciente p
                    JOIN p.usuario u
                    WHERE p.excluidoEm IS NULL
                      AND u.excluidoEm IS NULL
                      AND p.grauSuporte = :grauSuporte
                    """
    )
    Page<PacienteResumoProjection> buscarAtivosPorGrau(
            @Param("grauSuporte") GrauSuporte grauSuporte,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT new br.org.amde.digital.dto.paciente.PacienteResumoProjection(
                        p.id, u.id, u.nome, u.email, p.grauSuporte, u.numCelular
                    )
                    FROM Paciente p
                    JOIN p.usuario u
                    WHERE p.excluidoEm IS NULL
                      AND u.excluidoEm IS NULL
                      AND (
                          cast(function('public.unaccent', lower(u.nome)) as string) LIKE :padraoLike ESCAPE '!'
                          OR cast(function('public.unaccent', lower(u.email)) as string) LIKE :padraoLike ESCAPE '!'
                      )
                    """,
            countQuery = """
                    SELECT COUNT(p)
                    FROM Paciente p
                    JOIN p.usuario u
                    WHERE p.excluidoEm IS NULL
                      AND u.excluidoEm IS NULL
                      AND (
                          cast(function('public.unaccent', lower(u.nome)) as string) LIKE :padraoLike ESCAPE '!'
                          OR cast(function('public.unaccent', lower(u.email)) as string) LIKE :padraoLike ESCAPE '!'
                      )
                    """
    )
    Page<PacienteResumoProjection> buscarAtivosPorTermo(
            @Param("padraoLike") String padraoLike,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT new br.org.amde.digital.dto.paciente.PacienteResumoProjection(
                        p.id, u.id, u.nome, u.email, p.grauSuporte, u.numCelular
                    )
                    FROM Paciente p
                    JOIN p.usuario u
                    WHERE p.excluidoEm IS NULL
                      AND u.excluidoEm IS NULL
                      AND p.grauSuporte = :grauSuporte
                      AND (
                          cast(function('public.unaccent', lower(u.nome)) as string) LIKE :padraoLike ESCAPE '!'
                          OR cast(function('public.unaccent', lower(u.email)) as string) LIKE :padraoLike ESCAPE '!'
                      )
                    """,
            countQuery = """
                    SELECT COUNT(p)
                    FROM Paciente p
                    JOIN p.usuario u
                    WHERE p.excluidoEm IS NULL
                      AND u.excluidoEm IS NULL
                      AND p.grauSuporte = :grauSuporte
                      AND (
                          cast(function('public.unaccent', lower(u.nome)) as string) LIKE :padraoLike ESCAPE '!'
                          OR cast(function('public.unaccent', lower(u.email)) as string) LIKE :padraoLike ESCAPE '!'
                      )
                    """
    )
    Page<PacienteResumoProjection> buscarAtivosPorTermoEGrau(
            @Param("padraoLike") String padraoLike,
            @Param("grauSuporte") GrauSuporte grauSuporte,
            Pageable pageable
    );
}
