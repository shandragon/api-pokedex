package br.edu.shandragon.pokedex.usuario.repository;

import br.edu.shandragon.pokedex.compartilhado.UuidUtil;
import br.edu.shandragon.pokedex.usuario.entity.Usuario;
import br.edu.shandragon.pokedex.usuario.repository.jpa.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryIntegrationTest {

    @Autowired
    private UsuarioRepository repositorio;

    @Test
    void devePersistirERecuperarUsuarioPorId() {
        var usuario = new Usuario(UuidUtil.gerarV7(), "Ash Ketchum", "ash@pokemon.com", "hash123", Instant.now(), true, 0, null);
        repositorio.save(usuario);

        var encontrado = repositorio.findById(usuario.getId());
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNome()).isEqualTo("Ash Ketchum");
        assertThat(encontrado.get().getEmail()).isEqualTo("ash@pokemon.com");
    }

    @Test
    void deveRejeitarEmailDuplicado() {
        var u1 = new Usuario(UuidUtil.gerarV7(), "Ash", "ash@pokemon.com", "hash1", Instant.now(), true, 0, null);
        var u2 = new Usuario(UuidUtil.gerarV7(), "Misty", "ash@pokemon.com", "hash2", Instant.now(), true, 0, null);
        repositorio.save(u1);

        assertThatThrownBy(() -> {
            repositorio.saveAndFlush(u2);
        }).isInstanceOf(Exception.class);
    }

    @Test
    void deveVerificarExistenciaPorEmail() {
        var usuario = new Usuario(UuidUtil.gerarV7(), "Brock", "brock@pokemon.com", "hash", Instant.now(), true, 0, null);
        repositorio.save(usuario);

        assertThat(repositorio.existsByEmail("brock@pokemon.com")).isTrue();
        assertThat(repositorio.existsByEmail("naoexiste@pokemon.com")).isFalse();
    }

    @Test
    void deveBuscarUsuarioPorEmail() {
        var usuario = new Usuario(UuidUtil.gerarV7(), "Misty", "misty@pokemon.com", "hash", Instant.now(), true, 0, null);
        repositorio.save(usuario);

        var encontrado = repositorio.findByEmail("misty@pokemon.com");
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNome()).isEqualTo("Misty");
    }

    @Test
    void devePersistirCamposDeControleDeAcesso() {
        var usuario = new Usuario(UuidUtil.gerarV7(), "Gary", "gary@pokemon.com", "hash", Instant.now(), false, 3, Instant.now().plusSeconds(900));
        repositorio.save(usuario);

        var encontrado = repositorio.findById(usuario.getId()).orElseThrow();
        assertThat(encontrado.isAtivo()).isFalse();
        assertThat(encontrado.getTentativasFalhas()).isEqualTo(3);
        assertThat(encontrado.getBloqueadoAte()).isNotNull();
    }

    @Test
    void devePersistirUsuarioAtivoPorPadrao() {
        var usuario = new Usuario(UuidUtil.gerarV7(), "Red", "red@pokemon.com", "hash", Instant.now(), true, 0, null);
        repositorio.save(usuario);

        var encontrado = repositorio.findById(usuario.getId()).orElseThrow();
        assertThat(encontrado.isAtivo()).isTrue();
        assertThat(encontrado.getTentativasFalhas()).isZero();
        assertThat(encontrado.getBloqueadoAte()).isNull();
    }
}
