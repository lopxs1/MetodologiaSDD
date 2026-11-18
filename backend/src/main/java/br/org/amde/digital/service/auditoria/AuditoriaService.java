package br.org.amde.digital.service.auditoria;

import br.org.amde.digital.domain.entity.LogAuditoria;
import br.org.amde.digital.domain.enums.AcaoAuditoria;
import br.org.amde.digital.exception.ErroAuditoriaPreviaException;
import br.org.amde.digital.repository.LogAuditoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Servico transversal de auditoria (SPEC-005 / UC20 / RN16, RN44-RN48).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final ApplicationEventPublisher eventPublisher;
    private final LogAuditoriaRepository logAuditoriaRepository;
    private final ClientIpResolver clientIpResolver;
    private final RecursoAuditoriaFormatter recursoFormatter;

    public void registrarAsync(Long usuarioId, AcaoAuditoria acao, String recurso) {
        registrarAsync(usuarioId, acao, recurso, clientIpResolver.resolveClientIp());
    }

    public void registrarAsync(Long usuarioId, AcaoAuditoria acao, String recurso, String ipOrigem) {
        if (usuarioId == null || acao == null) {
            log.error("Evento de auditoria ignorado por ausencia de usuarioId ou acao");
            return;
        }
        try {
            String recursoValidado = recursoFormatter.validarRecurso(recurso);
            OffsetDateTime instanteOperacao = OffsetDateTime.now();
            eventPublisher.publishEvent(
                    new AuditoriaEventoCommand(usuarioId, acao, recursoValidado, ipOrigem, instanteOperacao)
            );
        } catch (Exception ex) {
            log.error("Falha tecnica ao enfileirar evento de auditoria [acao={}, erro={}]",
                    acao, ex.getClass().getSimpleName());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public LogAuditoria registrarSincronoPreExclusao(Long usuarioId, String recurso, String ipOrigem) {
        if (usuarioId == null) {
            throw new ErroAuditoriaPreviaException("Falha de auditoria previa: usuarioId ausente", null);
        }
        try {
            String recursoValidado = recursoFormatter.validarRecurso(recurso);
            OffsetDateTime instanteOperacao = OffsetDateTime.now();
            LogAuditoria registro = new LogAuditoria(
                    usuarioId,
                    AcaoAuditoria.EXCLUSAO,
                    recursoValidado,
                    ipOrigem,
                    instanteOperacao
            );
            return logAuditoriaRepository.save(registro);
        } catch (Exception ex) {
            log.error("Falha tecnica critica na auditoria previa de exclusao [erro={}]",
                    ex.getClass().getSimpleName());
            throw new ErroAuditoriaPreviaException(
                    "Não foi possível concluir a operação devido a uma falha interna de auditoria", ex);
        }
    }
}
