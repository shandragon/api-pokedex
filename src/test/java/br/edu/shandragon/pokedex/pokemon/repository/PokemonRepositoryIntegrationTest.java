package br.edu.shandragon.pokedex.pokemon.repository;

import br.edu.shandragon.pokedex.compartilhado.UuidUtil;
import br.edu.shandragon.pokedex.pokemon.entity.Pokemon;
import br.edu.shandragon.pokedex.pokemon.entity.Tipo;
import br.edu.shandragon.pokedex.pokemon.repository.jpa.PokemonRepository;
import br.edu.shandragon.pokedex.pokemon.repository.jpa.TipoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class PokemonRepositoryIntegrationTest {

    @Autowired
    private PokemonRepository pokemonRepositorio;

    @Autowired
    private TipoRepository tipoRepositorio;

    @Test
    void devePersistirPokemonComUuidV7() {
        var tipo = tipoRepositorio.save(new Tipo(UuidUtil.gerarV7(), "Planta"));
        var pokemon = new Pokemon(UuidUtil.gerarV7(), 1, "Bulbasaur", Set.of(tipo));
        pokemonRepositorio.save(pokemon);

        var encontrado = pokemonRepositorio.findById(pokemon.getId());
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNome()).isEqualTo("Bulbasaur");
        assertThat(encontrado.get().getNumeroPokdex()).isEqualTo(1);
    }

    @Test
    void deveGarantirUnicidadeDeNumeroPokdex() {
        var id1 = UuidUtil.gerarV7();
        var id2 = UuidUtil.gerarV7();
        pokemonRepositorio.save(new Pokemon(id1, 1, "Bulbasaur", Set.of()));

        assertThatThrownBy(() -> pokemonRepositorio.saveAndFlush(new Pokemon(id2, 1, "Ivysaur", Set.of())))
                .isInstanceOf(Exception.class);
    }

    @Test
    void deveVerificarExistenciaPorNumeroPokdex() {
        pokemonRepositorio.save(new Pokemon(UuidUtil.gerarV7(), 25, "Pikachu", Set.of()));

        assertThat(pokemonRepositorio.existsByNumeroPokdex(25)).isTrue();
        assertThat(pokemonRepositorio.existsByNumeroPokdex(99)).isFalse();
    }
}
