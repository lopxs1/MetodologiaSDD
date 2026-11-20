package br.org.amde.digital.service.auditoria;

import br.org.amde.digital.domain.entity.LogAuditoria;
import br.org.amde.digital.repository.LogAuditoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Consumidor assincrono pos-commit para eventos de auditoria (RN44, RNF-06.1, RNF-06.2).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditoriaEventListener {

    private final LogAuditoriaRepository logAuditoriaRepository;

    @Async("auditoriaTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processarEvento(AuditoriaEventoCommand command) {
        try {
            LogAuditoria registro = new LogAuditoria(
                    command.usuarioId(),
                    command.acao(),
                    command.recurso(),
                    command.ipOrigem(),
                    command.criadoEm()
            );
            logAuditoriaRepository.save(registro);
        } catch (Exception ex) {
            // RN44 / RNF-06.4: log tecnico sem dados pessoais, titulos ou conteudo clinico
            log.error("Falha tecnica ao persistir log de auditoria [acao={}, erro={}]",
                    command.acao(), ex.getClass().getSimpleName());
        }
    }
}
