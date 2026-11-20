package br.org.amde.digital.security;

import br.org.amde.digital.domain.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Servico JWT stateless (HS256, validade fixa de 8h sem renovacao automatica — RN20, RNF-01.2).
 */
@Service
public class JwtService {

    private static final ZoneId ZONE_SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtService(
            @Value("${amde.security.jwt.secret}") String secret,
            @Value("${amde.security.jwt.expiration-ms:28800000}") long expirationMs
    ) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET deve possuir ao menos 256 bits (32 bytes)");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public TokenGerado gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiracao = agora.plusMillis(expirationMs);
        String token = Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("email", usuario.getEmail())
                .claim("nivelAcesso", usuario.getNivelAcesso().name())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiracao))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
        return new TokenGerado(token, OffsetDateTime.ofInstant(expiracao, ZONE_SAO_PAULO));
    }

    public Claims validarEExtrairClaims(String token) throws JwtException, IllegalArgumentException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extrairUsuarioId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    public record TokenGerado(String token, OffsetDateTime expiraEm) {
    }
}
