package br.org.amde.digital.service.auditoria;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIpResolverTest {

    private final ClientIpResolver resolver = new ClientIpResolver("127.0.0.1,::1");

    @AfterEach
    void limparContexto() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void deveResolverIpDiretoQuandoNaoHaProxyConfiavel() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.10.25");
        request.addHeader("X-Forwarded-For", "10.0.0.99");

        assertThat(resolver.resolveClientIp(request)).isEqualTo("192.168.10.25");
    }

    @Test
    void deveUsarPrimeiroIpDoXForwardedForQuandoRemoteAddrEhProxyConfiavel() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Forwarded-For", "203.0.113.45, 10.0.0.1");

        assertThat(resolver.resolveClientIp(request)).isEqualTo("203.0.113.45");
    }

    @Test
    void deveRetornarRemoteAddrQuandoXForwardedForEhInvalido() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("::1");
        request.addHeader("X-Forwarded-For", "ip-invalido");

        assertThat(resolver.resolveClientIp(request)).isEqualTo("::1");
    }

    @Test
    void deveRetornarNuloSemRequisicaoHttpOuResolverViaContextHolder() {
        assertThat(resolver.resolveClientIp(null)).isNull();
        assertThat(resolver.resolveClientIp()).isNull();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.16.0.10");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertThat(resolver.resolveClientIp()).isEqualTo("172.16.0.10");
    }
}
