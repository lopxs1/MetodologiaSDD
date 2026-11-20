package br.org.amde.digital.repository;

import br.org.amde.digital.dto.dashboard.ConsultaDiaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DashboardRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public record ContagemConsultas(long totalConsultas, long consultasHoje) {
    }

    public record ContagemAtivos(long totalPacientes, long profissionaisAtivos) {
    }

    public record ContagemEspecialidade(String especialidade, long quantidade) {
    }

    public boolean existeFuncionario(Long funcionarioId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM amde_digital.funcionario f WHERE f.id = :funcionarioId)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("funcionarioId", funcionarioId, Types.BIGINT);
        Boolean existe = jdbcTemplate.queryForObject(sql, params, Boolean.class);
        return Boolean.TRUE.equals(existe);
    }

    public ContagemConsultas contarConsultas(
            OffsetDateTime inicioPeriodo,
            OffsetDateTime fimPeriodo,
            Long funcionarioId,
            OffsetDateTime inicioHojeSp,
            OffsetDateTime fimHojeSp
    ) {
        String sql = """
                SELECT
                    COUNT(*) AS total_consultas,
                    COUNT(*) FILTER (
                        WHERE a.data_inicio >= :inicioHojeSp
                          AND a.data_inicio < :fimHojeSp
                    ) AS consultas_hoje
                FROM amde_digital.agendamento a
                WHERE a.status <> 'CANCELADO'
                  AND (CAST(:funcionarioId AS bigint) IS NULL OR a.funcionario_id = CAST(:funcionarioId AS bigint))
                  AND (CAST(:inicioPeriodo AS timestamptz) IS NULL OR a.data_inicio >= CAST(:inicioPeriodo AS timestamptz))
                  AND (CAST(:fimPeriodo AS timestamptz) IS NULL OR a.data_inicio < CAST(:fimPeriodo AS timestamptz))
                """;
        MapSqlParameterSource params = criarFiltros(inicioPeriodo, fimPeriodo, funcionarioId)
                .addValue("inicioHojeSp", inicioHojeSp, Types.TIMESTAMP_WITH_TIMEZONE)
                .addValue("fimHojeSp", fimHojeSp, Types.TIMESTAMP_WITH_TIMEZONE);

        return jdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> new ContagemConsultas(
                rs.getLong("total_consultas"),
                rs.getLong("consultas_hoje")
        ));
    }

    public ContagemAtivos contarAtivos() {
        String sql = """
                SELECT
                    (
                        SELECT COUNT(*)
                        FROM amde_digital.paciente p
                        INNER JOIN amde_digital.usuario u ON u.id = p.usuario_id
                        WHERE p.excluido_em IS NULL AND u.excluido_em IS NULL
                    ) AS total_pacientes,
                    (
                        SELECT COUNT(*)
                        FROM amde_digital.funcionario f
                        INNER JOIN amde_digital.usuario u ON u.id = f.usuario_id
                        WHERE f.excluido_em IS NULL AND u.excluido_em IS NULL
                    ) AS profissionais_ativos
                """;
        return jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), (rs, rowNum) -> new ContagemAtivos(
                rs.getLong("total_pacientes"),
                rs.getLong("profissionais_ativos")
        ));
    }

    public List<ConsultaDiaDTO> buscarConsultasAoLongoDoTempo(
            OffsetDateTime inicioPeriodo,
            OffsetDateTime fimPeriodo,
            Long funcionarioId
    ) {
        String sql = """
                SELECT
                    (a.data_inicio AT TIME ZONE 'America/Sao_Paulo')::date AS data,
                    COUNT(*) AS quantidade
                FROM amde_digital.agendamento a
                WHERE a.status <> 'CANCELADO'
                  AND (CAST(:funcionarioId AS bigint) IS NULL OR a.funcionario_id = CAST(:funcionarioId AS bigint))
                  AND (CAST(:inicioPeriodo AS timestamptz) IS NULL OR a.data_inicio >= CAST(:inicioPeriodo AS timestamptz))
                  AND (CAST(:fimPeriodo AS timestamptz) IS NULL OR a.data_inicio < CAST(:fimPeriodo AS timestamptz))
                GROUP BY (a.data_inicio AT TIME ZONE 'America/Sao_Paulo')::date
                ORDER BY data ASC
                """;
        MapSqlParameterSource params = criarFiltros(inicioPeriodo, fimPeriodo, funcionarioId);
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new ConsultaDiaDTO(
                rs.getObject("data", LocalDate.class),
                rs.getLong("quantidade")
        ));
    }

    public List<ContagemEspecialidade> buscarConsultasPorEspecialidade(
            OffsetDateTime inicioPeriodo,
            OffsetDateTime fimPeriodo,
            Long funcionarioId
    ) {
        String sql = """
                WITH rotulos AS (
                    SELECT DISTINCT ON (lower(trim(f.cargo)))
                        lower(trim(f.cargo)) AS chave_cargo,
                        trim(f.cargo) AS rotulo_oficial
                    FROM amde_digital.funcionario f
                    ORDER BY lower(trim(f.cargo)), f.id ASC
                )
                SELECT
                    r.rotulo_oficial AS especialidade,
                    COUNT(*) AS quantidade
                FROM amde_digital.agendamento a
                INNER JOIN amde_digital.funcionario f ON f.id = a.funcionario_id
                INNER JOIN rotulos r ON r.chave_cargo = lower(trim(f.cargo))
                WHERE a.status <> 'CANCELADO'
                  AND (CAST(:funcionarioId AS bigint) IS NULL OR a.funcionario_id = CAST(:funcionarioId AS bigint))
                  AND (CAST(:inicioPeriodo AS timestamptz) IS NULL OR a.data_inicio >= CAST(:inicioPeriodo AS timestamptz))
                  AND (CAST(:fimPeriodo AS timestamptz) IS NULL OR a.data_inicio < CAST(:fimPeriodo AS timestamptz))
                GROUP BY r.rotulo_oficial
                ORDER BY quantidade DESC, lower(r.rotulo_oficial) ASC
                """;
        MapSqlParameterSource params = criarFiltros(inicioPeriodo, fimPeriodo, funcionarioId);
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new ContagemEspecialidade(
                rs.getString("especialidade"),
                rs.getLong("quantidade")
        ));
    }

    private MapSqlParameterSource criarFiltros(
            OffsetDateTime inicioPeriodo,
            OffsetDateTime fimPeriodo,
            Long funcionarioId
    ) {
        return new MapSqlParameterSource()
                .addValue("inicioPeriodo", inicioPeriodo, Types.TIMESTAMP_WITH_TIMEZONE)
                .addValue("fimPeriodo", fimPeriodo, Types.TIMESTAMP_WITH_TIMEZONE)
                .addValue("funcionarioId", funcionarioId, Types.BIGINT);
    }
}

