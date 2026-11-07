package com.example.service;

import com.example.dto.UsuarioDTO;
import com.example.model.Perfil;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class TokenJwtService {

    private static final long DEFAULT_DURATION_SECONDS = 28_800L; // 8 horas

    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "psi-jwt")
    String issuer = "psi-jwt";

    @ConfigProperty(name = "psi.jwt.duration-seconds", defaultValue = "28800")
    long durationSeconds = DEFAULT_DURATION_SECONDS;

    public String generateJwt(UsuarioDTO usuario) {
        Instant now = Instant.now();
        Instant expiration = now.plus(Duration.ofSeconds(getDurationSeconds()));

        Set<String> groups = usuario.perfis() == null
                ? Set.of()
                : usuario.perfis().stream()
                        .map(Perfil::name)
                        .collect(Collectors.toSet());

        return Jwt.issuer(issuer != null ? issuer : "psi-jwt")
                .upn(usuario.login())
                .subject(usuario.id() != null ? String.valueOf(usuario.id()) : usuario.login())
                .groups(groups)
                .claim("userId", usuario.id())
                .claim("nome", usuario.nome())
                .claim("email", usuario.email())
                .issuedAt(now)
                .expiresAt(expiration)
                .sign();
    }

    public long getDurationSeconds() {
        return durationSeconds > 0 ? durationSeconds : DEFAULT_DURATION_SECONDS;
    }
}
