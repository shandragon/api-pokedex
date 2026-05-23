package br.edu.shandragon.pokedex.usuario.controller;

import br.edu.shandragon.pokedex.usuario.dto.UsuarioRequisicaoDTO;
import br.edu.shandragon.pokedex.usuario.dto.UsuarioRespostaDTO;
import br.edu.shandragon.pokedex.usuario.dto.UsuarioRespostaPublicaDTO;
import br.edu.shandragon.pokedex.usuario.service.UsuarioService;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@Import(SegurancaConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "app.seguranca.token-admin=token-teste")
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioService servico;

    private static final String TOKEN = "token-teste";
    private static final String CABECALHO_AUTH = "Authorization";

    @Test
    void postSemTokenDeveRetornar401() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ash\",\"email\":\"ash@p.com\",\"senha\":\"123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postComTokenInvalidoDeveRetornar401() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .header(CABECALHO_AUTH, "Bearer token-errado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ash\",\"email\":\"ash@p.com\",\"senha\":\"123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postComTokenValidoDeveRetornar201SemSenha() throws Exception {
        var id = UUID.randomUUID();
        var resposta = new UsuarioRespostaDTO(id, "Ash Ketchum", "ash@pokemon.com", Instant.now());
        when(servico.cadastrar(any(UsuarioRequisicaoDTO.class))).thenReturn(resposta);

        mockMvc.perform(post("/api/usuarios")
                        .header(CABECALHO_AUTH, "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ash Ketchum\",\"email\":\"ash@pokemon.com\",\"senha\":\"senha123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Ash Ketchum"))
                .andExpect(jsonPath("$.email").value("ash@pokemon.com"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void postComEmailDuplicadoDeveRetornar409() throws Exception {
        when(servico.cadastrar(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado"));

        mockMvc.perform(post("/api/usuarios")
                        .header(CABECALHO_AUTH, "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ash\",\"email\":\"ash@pokemon.com\",\"senha\":\"123\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void getListarSemTokenDeveRetornarApenasIdENome() throws Exception {
        var id = UUID.randomUUID();
        when(servico.listar()).thenReturn(List.of(new UsuarioRespostaPublicaDTO(id, "Ash Ketchum")));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].nome").value("Ash Ketchum"))
                .andExpect(jsonPath("$[0].email").doesNotExist());
    }

    @Test
    void getBuscarPorIdInexistenteDeveRetornar404() throws Exception {
        when(servico.buscarPorId(any(UUID.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/usuarios/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
