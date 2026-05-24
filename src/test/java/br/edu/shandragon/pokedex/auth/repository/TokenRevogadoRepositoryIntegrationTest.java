package br.edu.shandragon.pokedex.auth.repository;

import br.edu.shandragon.pokedex.auth.entity.TokenRevogado;
import br.edu.shandragon.pokedex.auth.repository.jpa.TokenRevogadoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TokenRevogadoRepositoryIntegrationTest {

    @Autowired
    private TokenRevogadoRepository repositorio;

    @Test
    void deveRetornarTrueParaJtiExistente() {
        UUID jti = UUID.randomUUID();
        repositorio.save(new TokenRevogado(jti, Instant.now().plusSeconds(3600)));

        assertThat(repositorio.existsByJti(jti)).isTrue();
    }

    @Test
    void deveRetornarFalseParaJtiInexistente() {
        UUID jtiInexistente = UUID.randomUUID();

        assertThat(repositorio.existsByJti(jtiInexistente)).isFalse();
    }

    @Test
    void devePersistirERecuperarTokenRevogado() {
        UUID jti = UUID.randomUUID();
        Instant expiraEm = Instant.now().plusSeconds(3600);
        repositorio.save(new TokenRevogado(jti, expiraEm));

        var encontrado = repositorio.findById(jti);
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getExpiraEm()).isEqualTo(expiraEm);
    }
}
