package br.edu.shandragon.pokedex;

import br.edu.shandragon.pokedex.model.Pokemon;
import br.edu.shandragon.pokedex.model.Tipo;
import br.edu.shandragon.pokedex.repository.PokemonRepository;
import br.edu.shandragon.pokedex.repository.TipoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PokedexDesempenhoTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PokemonRepository pokemonRepository;

    @Autowired
    private TipoRepository tipoRepository;

    @BeforeEach
    void seed() {
        pokemonRepository.deleteAll();
        tipoRepository.deleteAll();

        Tipo fogo = tipoRepository.save(new Tipo(null, "Fogo"));
        Tipo agua = tipoRepository.save(new Tipo(null, "Agua"));
        Tipo planta = tipoRepository.save(new Tipo(null, "Planta"));
        Tipo[] tipos = {fogo, agua, planta};

        pokemonRepository.saveAll(
            IntStream.rangeClosed(1, 1000)
                .mapToObj(i -> new Pokemon(null, "Pokemon-" + i, Set.of(tipos[i % 3])))
                .collect(Collectors.toList())
        );
    }

    @AfterEach
    void cleanup() {
        pokemonRepository.deleteAll();
        tipoRepository.deleteAll();
    }

    @Test
    void listarPorTipo_deveResponderEmMenosDe500ms() {
        restTemplate.getForEntity("/api/pokedex/por-tipo", String.class);

        long inicio = System.currentTimeMillis();
        ResponseEntity<String> resposta = restTemplate.getForEntity("/api/pokedex/por-tipo", String.class);
        long duracao = System.currentTimeMillis() - inicio;

        assertThat(resposta.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(duracao)
                .as("Tempo de resposta para 1.000 registros deve ser < 500ms, mas foi %dms", duracao)
                .isLessThan(500L);
    }
}
