package br.org.amde.digital.service.auditoria;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Resolve o endereco IP de origem (IPv4/IPv6, 3..45 caracteres), confiando em X-Forwarded-For
 * apenas quando a conexao direta provem de proxy confiavel configurado (SPEC-005, RN45).
 */
@Component
public class ClientIpResolver {

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$"
    );
    private static final Pattern IPV6_PATTERN = Pattern.compile("^[0-9a-fA-F:]{3,45}$");

    private final Set<String> trustedProxies;

    public ClientIpResolver(@Value("${amde.security.trusted-proxies:127.0.0.1,::1,0:0:0:0:0:0:0:1}") String proxies) {
        this.trustedProxies = Arrays.stream(proxies.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    public String resolveClientIp() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            return resolveClientIp(servletAttributes.getRequest());
        }
        return null;
    }

    public String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String remoteAddr = normalizarIpValido(request.getRemoteAddr());
        if (remoteAddr != null && trustedProxies.contains(remoteAddr)) {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                String primeiroIp = forwardedFor.split(",")[0];
                String ipValidado = normalizarIpValido(primeiroIp);
                if (ipValidado != null) {
                    return ipValidado;
                }
            }
        }
        return remoteAddr;
    }

    private String normalizarIpValido(String rawIp) {
        if (rawIp == null) {
            return null;
        }
        String trimmed = rawIp.trim();
        if (trimmed.length() < 3 || trimmed.length() > 45) {
            return null;
        }
        if (IPV4_PATTERN.matcher(trimmed).matches()) {
            return trimmed;
        }
        if (trimmed.contains(":") && IPV6_PATTERN.matcher(trimmed).matches()) {
            return trimmed;
        }
        return null;
    }
}
