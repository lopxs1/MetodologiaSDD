package br.org.amde.digital.service.auditoria;

import br.org.amde.digital.domain.enums.AcaoAuditoria;

import java.time.OffsetDateTime;

/**
 * Evento interno imutavel com instante capturado sincronamente na thread da operacao (RNF-06.5).
 */
public record AuditoriaEventoCommand(
        Long usuarioId,
        AcaoAuditoria acao,
        String recurso,
        String ipOrigem,
        OffsetDateTime criadoEm
) {
}
