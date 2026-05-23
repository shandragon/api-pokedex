package br.edu.shandragon.pokedex.pokemon.repository;

import br.edu.shandragon.pokedex.pokemon.document.PokemonAtributos;
import br.edu.shandragon.pokedex.pokemon.repository.mongo.PokemonAtributosRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
class PokemonAtributosRepositoryIntegrationTest {

    @Autowired
    private PokemonAtributosRepository repositorio;

    @Test
    void devePersistirERecuperarAtributosFlexiveis() {
        var id = UUID.randomUUID().toString();
        var atributos = new PokemonAtributos(id, Map.of(
                "ataques", java.util.List.of("Investida", "Absorver"),
                "fraquezas", java.util.List.of("Fogo", "Gelo"),
                "estatisticas", Map.of("hp", 45, "ataque", 49)
        ));
        repositorio.save(atributos);

        var encontrado = repositorio.findById(id);
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getAtributos()).containsKey("ataques");
        assertThat(encontrado.get().getAtributos()).containsKey("fraquezas");
        assertThat(encontrado.get().getAtributos()).containsKey("estatisticas");
    }

    @Test
    void devePersistirCamposArbitrarios() {
        var id = UUID.randomUUID().toString();
        var atributos = new PokemonAtributos(id, Map.of(
                "forma_alternativa", "Mega Bulbasaur",
                "geracao", 1,
                "lendario", false
        ));
        repositorio.save(atributos);

        var encontrado = repositorio.findById(id);
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getAtributos().get("forma_alternativa")).isEqualTo("Mega Bulbasaur");
    }

    @Test
    void deveRetornarVazioParaIdInexistente() {
        assertThat(repositorio.findById("id-inexistente")).isEmpty();
    }
}
