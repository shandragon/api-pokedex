package br.edu.shandragon.pokedex.pokemon.controller;

import br.edu.shandragon.pokedex.pokemon.dto.PokemonRequisicaoDTO;
import br.edu.shandragon.pokedex.pokemon.dto.PokemonRespostaDTO;
import br.edu.shandragon.pokedex.pokemon.service.PokemonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.edu.shandragon.pokedex.config.SegurancaConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PokemonController.class)
@Import(SegurancaConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "app.seguranca.token-admin=token-teste")
class PokemonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PokemonService servico;

    private static final String TOKEN = "token-teste";
    private static final String CABECALHO_AUTH = "Authorization";

    private PokemonRespostaDTO respostaExemplo() {
        return new PokemonRespostaDTO(UUID.randomUUID().toString(), 1, "Bulbasaur",
                List.of("Planta", "Veneno"), List.of(), Map.of("ataques", List.of("Absorver")));
    }

    @Test
    void postSemTokenDeveRetornar401() throws Exception {
        mockMvc.perform(post("/api/pokemon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroPokdex\":1,\"nome\":\"Bulbasaur\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postComTokenValidoEBodyValidoDeveRetornar201() throws Exception {
        when(servico.criar(any(PokemonRequisicaoDTO.class))).thenReturn(respostaExemplo());

        mockMvc.perform(post("/api/pokemon")
                        .header(CABECALHO_AUTH, "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroPokdex\":1,\"nome\":\"Bulbasaur\",\"tipos\":[\"Planta\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Bulbasaur"));
    }

    @Test
    void postComNumeroPokdexDuplicadoDeveRetornar409() throws Exception {
        when(servico.criar(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Número do Pokédex já existe"));

        mockMvc.perform(post("/api/pokemon")
                        .header(CABECALHO_AUTH, "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroPokdex\":1,\"nome\":\"Bulbasaur\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void postComTipoInvalidoDeveRetornar400() throws Exception {
        when(servico.criar(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo inválido"));

        mockMvc.perform(post("/api/pokemon")
                        .header(CABECALHO_AUTH, "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroPokdex\":1,\"nome\":\"Bulbasaur\",\"tipos\":[\"TipoInexistente\"]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getListarDeveRetornar200SemAutenticacao() throws Exception {
        when(servico.listarTodos()).thenReturn(List.of(respostaExemplo()));

        mockMvc.perform(get("/api/pokemon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Bulbasaur"));
    }

    @Test
    void getBuscarPorIdInexistenteDeveRetornar404() throws Exception {
        when(servico.buscarPorId(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/pokemon/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getListarSemPokemonsDeveRetornarArrayVazio() throws Exception {
        when(servico.listarTodos()).thenReturn(List.of());

        mockMvc.perform(get("/api/pokemon"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void postComAtributosTopLevelDeveMapearParaAtributosExtras() throws Exception {
        when(servico.criar(any(PokemonRequisicaoDTO.class))).thenAnswer(invocation -> {
            PokemonRequisicaoDTO dto = invocation.getArgument(0);
            return new PokemonRespostaDTO(UUID.randomUUID().toString(), dto.numeroPokdex(),
                    dto.nome(), List.of(), List.of(), dto.atributosExtras());
        });

        mockMvc.perform(post("/api/pokemon")
                        .header(CABECALHO_AUTH, "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroPokdex\":1,\"nome\":\"Bulbasaur\",\"tipos\":[\"Planta\"]," +
                                "\"ataques\":[\"Absorver\"],\"fraquezas\":[\"Fogo\"]," +
                                "\"estatisticas\":{\"hp\":45,\"ataque\":49}}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.atributosExtras.ataques[0]").value("Absorver"))
                .andExpect(jsonPath("$.atributosExtras.fraquezas[0]").value("Fogo"))
                .andExpect(jsonPath("$.atributosExtras.estatisticas.hp").value(45));
    }
}
