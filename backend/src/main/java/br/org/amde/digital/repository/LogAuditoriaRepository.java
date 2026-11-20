package br.org.amde.digital.repository;

import br.org.amde.digital.domain.entity.LogAuditoria;
import br.org.amde.digital.domain.enums.AcaoAuditoria;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio estritamente append-only para {@link LogAuditoria} (RNF-06.3).
 * Nao expoe metodos de atualizacao ou exclusao.
 */
public interface LogAuditoriaRepository extends Repository<LogAuditoria, Long> {

    LogAuditoria save(LogAuditoria entity);

    Optional<LogAuditoria> findById(Long id);

    List<LogAuditoria> findAll();

    List<LogAuditoria> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);

    List<LogAuditoria> findByAcaoOrderByCriadoEmDesc(AcaoAuditoria acao);

    long count();
}
