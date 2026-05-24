package br.edu.shandragon.pokedex.auth.controller;

import br.edu.shandragon.pokedex.auth.dto.LoginRespostaDTO;
import br.edu.shandragon.pokedex.auth.service.AuthService;
import br.edu.shandragon.pokedex.auth.service.JwtService;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SegurancaConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "app.seguranca.token-admin=token-teste")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    // T013 — cenários de configuração de segurança

    @Test
    void loginDeveSerAcessivelSemAutorizacao() throws Exception {
        var resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();

        assertThat(resultado.getResponse().getStatus()).isNotEqualTo(401);
    }

    @Test
    void logoutSemAutorizacaoDeveRetornar401() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    // T019 — US1: login com credenciais válidas

    @Test
    void loginComCredenciaisValidasDeveRetornar200ComTokenEExpiraEm() throws Exception {
        var expiraEm = Instant.now().plusSeconds(3600);
        when(authService.login(any(), anyString()))
                .thenReturn(new LoginRespostaDTO("jwt-token-gerado", expiraEm));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"joao@example.com\",\"senha\":\"minhasenha123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-gerado"))
                .andExpect(jsonPath("$.expira_em").exists());
    }

    // T024 — US2: campos obrigatórios e credenciais inválidas

    @Test
    void loginComBodyVazioDeveRetornar400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginComEmailInvalidoDeveRetornar400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nao-e-email\",\"senha\":\"senha123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginComCredenciaisInvalidasDeveRetornar401ComMensagemGenerica() throws Exception {
        when(authService.login(any(), anyString()))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"joao@example.com\",\"senha\":\"senhaErrada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha inválidos"));
    }

    // T029 — US4: logout com JWT válido

    @Test
    void logoutComJwtValidoDeveRetornar204() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        String tokenFake = "jwt-valido-fake";

        when(jwtService.validar(tokenFake)).thenReturn(true);
        when(jwtService.extrairSub(tokenFake)).thenReturn(usuarioId.toString());
        when(jwtService.gerar(usuarioId)).thenReturn("token-renovado");
        doNothing().when(authService).logout(tokenFake);

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + tokenFake))
                .andExpect(status().isNoContent());
    }

    @Test
    void logoutSemBearerDeveRetornar401() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }
}
