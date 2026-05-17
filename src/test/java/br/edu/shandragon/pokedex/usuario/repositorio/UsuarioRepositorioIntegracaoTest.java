package br.edu.shandragon.pokedex.usuario.repositorio;

import br.edu.shandragon.pokedex.compartilhado.UuidUtil;
import br.edu.shandragon.pokedex.usuario.entidade.Usuario;
import br.edu.shandragon.pokedex.usuario.repositorio.jpa.UsuarioRepositorio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositorioIntegracaoTest {

    @Autowired
    private UsuarioRepositorio repositorio;

    @Test
    void devePersistirERecuperarUsuarioPorId() {
        var usuario = new Usuario(UuidUtil.gerarV7(), "Ash Ketchum", "ash@pokemon.com", "hash123", Instant.now());
        repositorio.save(usuario);

        var encontrado = repositorio.findById(usuario.getId());
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNome()).isEqualTo("Ash Ketchum");
        assertThat(encontrado.get().getEmail()).isEqualTo("ash@pokemon.com");
    }

    @Test
    void deveRejeitarEmailDuplicado() {
        var u1 = new Usuario(UuidUtil.gerarV7(), "Ash", "ash@pokemon.com", "hash1", Instant.now());
        var u2 = new Usuario(UuidUtil.gerarV7(), "Misty", "ash@pokemon.com", "hash2", Instant.now());
        repositorio.save(u1);

        assertThatThrownBy(() -> {
            repositorio.saveAndFlush(u2);
        }).isInstanceOf(Exception.class);
    }

    @Test
    void deveVerificarExistenciaPorEmail() {
        var usuario = new Usuario(UuidUtil.gerarV7(), "Brock", "brock@pokemon.com", "hash", Instant.now());
        repositorio.save(usuario);

        assertThat(repositorio.existsByEmail("brock@pokemon.com")).isTrue();
        assertThat(repositorio.existsByEmail("naoexiste@pokemon.com")).isFalse();
    }
}
