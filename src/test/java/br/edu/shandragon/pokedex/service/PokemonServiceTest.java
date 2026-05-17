package br.edu.shandragon.pokedex.service;

import br.edu.shandragon.pokedex.dto.EvolucaoDTO;
import br.edu.shandragon.pokedex.dto.PokemonDTO;
import br.edu.shandragon.pokedex.model.Evolucao;
import br.edu.shandragon.pokedex.model.Pokemon;
import br.edu.shandragon.pokedex.model.Tipo;
import br.edu.shandragon.pokedex.repository.EvolucaoRepository;
import br.edu.shandragon.pokedex.repository.PokemonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PokemonServiceTest {

    @Mock
    private PokemonRepository pokemonRepository;

    @Mock
    private EvolucaoRepository evolucaoRepository;

    @InjectMocks
    private PokemonService pokemonService;

    @Test
    void listarAgrupadoPorTipo_deveAgruparCorretamentePorTipo() {
        Tipo fogo = new Tipo(1L, "Fogo");
        Tipo voo = new Tipo(2L, "Voo");
        Pokemon charizard = new Pokemon(1L, "Charizard", Set.of(fogo, voo));
        Pokemon charmander = new Pokemon(2L, "Charmander", Set.of(fogo));
        when(pokemonRepository.findAll()).thenReturn(List.of(charizard, charmander));

        Map<String, List<PokemonDTO>> resultado = pokemonService.listarAgrupadoPorTipo();

        assertThat(resultado).containsKey("Fogo");
        assertThat(resultado.get("Fogo")).hasSize(2);
        assertThat(resultado).containsKey("Voo");
        assertThat(resultado.get("Voo")).hasSize(1);
    }

    @Test
    void listarAgrupadoPorTipo_pokemonComMultiplosTiposAparecerEmCadaCategoria() {
        Tipo fogo = new Tipo(1L, "Fogo");
        Tipo voo = new Tipo(2L, "Voo");
        Pokemon charizard = new Pokemon(1L, "Charizard", Set.of(fogo, voo));
        when(pokemonRepository.findAll()).thenReturn(List.of(charizard));

        Map<String, List<PokemonDTO>> resultado = pokemonService.listarAgrupadoPorTipo();

        assertThat(resultado.get("Fogo")).extracting(PokemonDTO::getNome).contains("Charizard");
        assertThat(resultado.get("Voo")).extracting(PokemonDTO::getNome).contains("Charizard");
    }

    @Test
    void listarAgrupadoPorTipo_deveRetornarMapaVazioSemPokemon() {
        when(pokemonRepository.findAll()).thenReturn(List.of());

        Map<String, List<PokemonDTO>> resultado = pokemonService.listarAgrupadoPorTipo();

        assertThat(resultado).isEmpty();
    }

    @Test
    void buscarEvolucoes_deveRetornarEvolucoes() {
        Pokemon charmander = new Pokemon(1L, "Charmander", Set.of());
        Pokemon charmeleon = new Pokemon(2L, "Charmeleon", Set.of());
        Evolucao evolucao = new Evolucao(1L, charmander, charmeleon);
        when(evolucaoRepository.findByPokemonOrigemIdOrPokemonDestinoId(1L, 1L))
                .thenReturn(List.of(evolucao));

        List<EvolucaoDTO> resultado = pokemonService.buscarEvolucoes(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getPokemonOrigem().getNome()).isEqualTo("Charmander");
        assertThat(resultado.get(0).getPokemonDestino().getNome()).isEqualTo("Charmeleon");
    }

    @Test
    void buscarEvolucoes_deveRetornarListaVaziaQuandoSemEvolucoes() {
        when(evolucaoRepository.findByPokemonOrigemIdOrPokemonDestinoId(99L, 99L))
                .thenReturn(List.of());

        List<EvolucaoDTO> resultado = pokemonService.buscarEvolucoes(99L);

        assertThat(resultado).isEmpty();
    }
}
