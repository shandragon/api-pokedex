package br.edu.shandragon.pokedex.auth.service;

import br.edu.shandragon.pokedex.auth.repository.jpa.TokenRevogadoRepository;
import br.edu.shandragon.pokedex.compartilhado.UuidUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey chave;
    private final long ttlHoras;
    private final TokenRevogadoRepository tokenRevogadoRepository;

    public JwtService(
            @Value("${app.seguranca.jwt-secret}") String segredo,
            @Value("${app.seguranca.token-ttl-horas}") long ttlHoras,
            TokenRevogadoRepository tokenRevogadoRepository) {
        this.chave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(segredo));
        this.ttlHoras = ttlHoras;
        this.tokenRevogadoRepository = tokenRevogadoRepository;
    }

    public String gerar(UUID usuarioId) {
        Instant agora = Instant.now();
        Instant expiracao = agora.plus(ttlHoras, ChronoUnit.HOURS);
        return Jwts.builder()
                .subject(usuarioId.toString())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiracao))
                .id(UuidUtil.gerarV7().toString())
                .signWith(chave)
                .compact();
    }

    public boolean validar(String token) {
        try {
            Claims claims = parsear(token);
            UUID jti = UUID.fromString(claims.getId());
            return !tokenRevogadoRepository.existsByJti(jti);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extrairSub(String token) {
        return parsear(token).getSubject();
    }

    public String extrairJti(String token) {
        return parsear(token).getId();
    }

    public Instant extrairExp(String token) {
        return parsear(token).getExpiration().toInstant();
    }

    private Claims parsear(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
