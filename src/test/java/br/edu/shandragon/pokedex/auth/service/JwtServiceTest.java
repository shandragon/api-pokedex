package br.edu.shandragon.pokedex.auth.service;

import br.edu.shandragon.pokedex.auth.repository.jpa.TokenRevogadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private TokenRevogadoRepository tokenRevogadoRepository;

    private JwtService jwtService;

    private static final String SEGREDO = "Y2hhdmUtand0LXBhcmEtdGVzdGVzLWRlLWRlc2Vudm9sdmltZW50by0yNTZi";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SEGREDO, 1L, tokenRevogadoRepository);
    }

    @Test
    void deveGerarTokenComClaimsCorretos() {
        UUID usuarioId = UUID.randomUUID();
        String token = jwtService.gerar(usuarioId);

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtService.extrairSub(token)).isEqualTo(usuarioId.toString());
        assertThat(jwtService.extrairJti(token)).isNotNull();
        assertThat(jwtService.extrairExp(token)).isAfter(Instant.now());
    }

    @Test
    void deveValidarTokenValido() {
        UUID usuarioId = UUID.randomUUID();
        String token = jwtService.gerar(usuarioId);
        UUID jti = UUID.fromString(jwtService.extrairJti(token));

        when(tokenRevogadoRepository.existsByJti(jti)).thenReturn(false);

        assertThat(jwtService.validar(token)).isTrue();
    }

    @Test
    void deveRejeitarTokenComAssinaturaInvalida() {
        String token = jwtService.gerar(UUID.randomUUID());
        String tokenInvalido = token.substring(0, token.lastIndexOf('.') + 1) + "assinaturaInvalida";

        assertThat(jwtService.validar(tokenInvalido)).isFalse();
    }

    @Test
    void deveRejeitarTokenRevogado() {
        UUID usuarioId = UUID.randomUUID();
        String token = jwtService.gerar(usuarioId);
        UUID jti = UUID.fromString(jwtService.extrairJti(token));

        when(tokenRevogadoRepository.existsByJti(jti)).thenReturn(true);

        assertThat(jwtService.validar(token)).isFalse();
    }

    @Test
    void deveRejeitarTokenExpirado() {
        JwtService jwtServiceCurto = new JwtService(SEGREDO, -1L, tokenRevogadoRepository);
        String token = jwtServiceCurto.gerar(UUID.randomUUID());

        assertThat(jwtService.validar(token)).isFalse();
    }

    @Test
    void deveExtrairSubCorretamente() {
        UUID usuarioId = UUID.randomUUID();
        String token = jwtService.gerar(usuarioId);

        assertThat(jwtService.extrairSub(token)).isEqualTo(usuarioId.toString());
    }

    @Test
    void deveExtrairJtiComoUuidValido() {
        String token = jwtService.gerar(UUID.randomUUID());

        String jti = jwtService.extrairJti(token);

        assertThat(jti).isNotNull();
        assertThatCode(() -> UUID.fromString(jti)).doesNotThrowAnyException();
    }

    @Test
    void deveExtrairExpDentroDoTtl() {
        String token = jwtService.gerar(UUID.randomUUID());

        Instant exp = jwtService.extrairExp(token);

        assertThat(exp).isAfter(Instant.now());
        assertThat(exp).isBefore(Instant.now().plus(2, ChronoUnit.HOURS));
    }
}
