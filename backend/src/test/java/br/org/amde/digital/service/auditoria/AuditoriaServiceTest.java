package br.org.amde.digital.service.auditoria;

import br.org.amde.digital.domain.entity.LogAuditoria;
import br.org.amde.digital.domain.enums.AcaoAuditoria;
import br.org.amde.digital.exception.ErroAuditoriaPreviaException;
import br.org.amde.digital.repository.LogAuditoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private LogAuditoriaRepository logAuditoriaRepository;

    @Mock
    private ClientIpResolver clientIpResolver;

    @Spy
    private RecursoAuditoriaFormatter recursoFormatter = new RecursoAuditoriaFormatter();

    @InjectMocks
    private AuditoriaService auditoriaService;

    @Test
    void devePublicarEventoAsyncComInstanteCapturadoNaThreadDaOperacao() {
        when(clientIpResolver.resolveClientIp()).thenReturn("192.168.0.10");

        auditoriaService.registrarAsync(7L, AcaoAuditoria.LOGIN, "usuario:7:LOGIN");

        ArgumentCaptor<AuditoriaEventoCommand> captor = ArgumentCaptor.forClass(AuditoriaEventoCommand.class);
        verify(eventPublisher).publishEvent(captor.capture());

        AuditoriaEventoCommand evento = captor.getValue();
        assertThat(evento.usuarioId()).isEqualTo(7L);
        assertThat(evento.acao()).isEqualTo(AcaoAuditoria.LOGIN);
        assertThat(evento.recurso()).isEqualTo("usuario:7:LOGIN");
        assertThat(evento.ipOrigem()).isEqualTo("192.168.0.10");
        assertThat(evento.criadoEm()).isNotNull();
    }

    @Test
    void naoDevePropagarExcecaoQuandoPublicacaoAsyncFalhar() {
        doThrow(new RuntimeException("Falha no event bus")).when(eventPublisher).publishEvent(any());

        assertThatCode(() -> auditoriaService.registrarAsync(7L, AcaoAuditoria.LOGIN, "usuario:7:LOGIN", "127.0.0.1"))
                .doesNotThrowAnyException();

        auditoriaService.registrarAsync(null, AcaoAuditoria.LOGIN, "usuario:7:LOGIN", "127.0.0.1");
        verify(eventPublisher, never()).publishEvent(argThatNullUsuario());
    }

    @Test
    void devePersistirAuditoriaSincronaPreExclusao() {
        when(logAuditoriaRepository.save(any(LogAuditoria.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        LogAuditoria salvo = auditoriaService.registrarSincronoPreExclusao(
                3L,
                "documento:12:EXCLUSAO",
                "10.0.0.5"
        );

        assertThat(salvo.getUsuarioId()).isEqualTo(3L);
        assertThat(salvo.getAcao()).isEqualTo(AcaoAuditoria.EXCLUSAO);
        assertThat(salvo.getRecurso()).isEqualTo("documento:12:EXCLUSAO");
        assertThat(salvo.getIpOrigem()).isEqualTo("10.0.0.5");
        assertThat(salvo.getCriadoEm()).isNotNull();
    }

    @Test
    void deveLancarErroAuditoriaPreviaQuandoPersistenciaFalharOuUsuarioForNulo() {
        assertThatThrownBy(() -> auditoriaService.registrarSincronoPreExclusao(null, "documento:12:EXCLUSAO", null))
                .isInstanceOf(ErroAuditoriaPreviaException.class);

        when(logAuditoriaRepository.save(any(LogAuditoria.class)))
                .thenThrow(new DataAccessResourceFailureException("Banco indisponivel"));

        assertThatThrownBy(() -> auditoriaService.registrarSincronoPreExclusao(3L, "documento:12:EXCLUSAO", "10.0.0.5"))
                .isInstanceOf(ErroAuditoriaPreviaException.class);
    }

    private Object argThatNullUsuario() {
        return org.mockito.ArgumentMatchers.argThat(arg ->
                arg instanceof AuditoriaEventoCommand cmd && cmd.usuarioId() == null);
    }
}
