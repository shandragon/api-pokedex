package br.edu.shandragon.pokedex.controller;

import br.edu.shandragon.pokedex.dto.EvolucaoDTO;
import br.edu.shandragon.pokedex.dto.PokemonDTO;
import br.edu.shandragon.pokedex.exception.PokemonNaoEncontradoException;
import br.edu.shandragon.pokedex.service.PokemonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PokemonController.class)
class PokemonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PokemonService pokemonService;

    @Test
    void listarPorTipo_deveRetornar200() throws Exception {
        when(pokemonService.listarAgrupadoPorTipo()).thenReturn(Map.of());

        mockMvc.perform(get("/api/pokedex/por-tipo"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void listarPorTipo_deveRetornarPokemonAgrupados() throws Exception {
        PokemonDTO pikachu = new PokemonDTO(1L, "Pikachu", Set.of());
        when(pokemonService.listarAgrupadoPorTipo())
                .thenReturn(Map.of("Fogo", List.of(pikachu)));

        mockMvc.perform(get("/api/pokedex/por-tipo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Fogo[0].nome").value("Pikachu"));
    }

    @Test
    void listarEvolucoes_deveRetornar200() throws Exception {
        when(pokemonService.buscarEvolucoes(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/pokedex/1/evolucoes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void listarEvolucoes_deveRetornarEvolucoes() throws Exception {
        PokemonDTO charmander = new PokemonDTO(1L, "Charmander", Set.of());
        PokemonDTO charmeleon = new PokemonDTO(2L, "Charmeleon", Set.of());
        EvolucaoDTO evolucao = new EvolucaoDTO(1L, charmander, charmeleon);
        when(pokemonService.buscarEvolucoes(1L)).thenReturn(List.of(evolucao));

        mockMvc.perform(get("/api/pokedex/1/evolucoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pokemonOrigem.nome").value("Charmander"))
                .andExpect(jsonPath("$[0].pokemonDestino.nome").value("Charmeleon"));
    }

    @Test
    void listarEvolucoes_deveRetornar404QuandoPokemonNaoExiste() throws Exception {
        when(pokemonService.buscarEvolucoes(99L))
                .thenThrow(new PokemonNaoEncontradoException(99L));

        mockMvc.perform(get("/api/pokedex/99/evolucoes"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").exists());
    }
}
