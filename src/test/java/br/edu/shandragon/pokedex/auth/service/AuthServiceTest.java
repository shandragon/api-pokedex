package br.edu.shandragon.pokedex.auth.service;

import br.edu.shandragon.pokedex.auth.dto.LoginRequisicaoDTO;
import br.edu.shandragon.pokedex.auth.entity.LogLogin;
import br.edu.shandragon.pokedex.auth.entity.TokenRevogado;
import br.edu.shandragon.pokedex.auth.repository.jpa.LogLoginRepository;
import br.edu.shandragon.pokedex.auth.repository.jpa.TokenRevogadoRepository;
import br.edu.shandragon.pokedex.usuario.entity.Usuario;
import br.edu.shandragon.pokedex.usuario.repository.jpa.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder codificadorDeSenha;

    @Mock
    private LogLoginRepository logLoginRepository;

    @Mock
    private TokenRevogadoRepository tokenRevogadoRepository;

    private AuthService authService;

    private Usuario usuarioAtivo;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioRepository, jwtService, codificadorDeSenha, logLoginRepository, tokenRevogadoRepository, 5, 15);
        usuarioAtivo = new Usuario(UUID.randomUUID(), "Ash", "ash@pokemon.com", "hashBcrypt", Instant.now(), true, 0, null);
    }

    // T018 — US1: login com credenciais válidas

    @Test
    void loginComCredenciaisValidasDeveRetornarTokenJwt() {
        when(usuarioRepository.findByEmail("ash@pokemon.com")).thenReturn(Optional.of(usuarioAtivo));
        when(codificadorDeSenha.matches("senha123", "hashBcrypt")).thenReturn(true);
        when(jwtService.gerar(usuarioAtivo.getId())).thenReturn("jwt-token-gerado");
        when(jwtService.extrairExp("jwt-token-gerado")).thenReturn(Instant.now().plusSeconds(3600));

        var resposta = authService.login(new LoginRequisicaoDTO("ash@pokemon.com", "senha123"), "127.0.0.1");

        assertThat(resposta.token()).isEqualTo("jwt-token-gerado");
        assertThat(resposta.expiraEm()).isAfter(Instant.now());
    }

    @Test
    void loginComCredenciaisValidasDeveSalvarLogSucesso() {
        when(usuarioRepository.findByEmail("ash@pokemon.com")).thenReturn(Optional.of(usuarioAtivo));
        when(codificadorDeSenha.matches("senha123", "hashBcrypt")).thenReturn(true);
        when(jwtService.gerar(usuarioAtivo.getId())).thenReturn("jwt-token-gerado");
        when(jwtService.extrairExp("jwt-token-gerado")).thenReturn(Instant.now().plusSeconds(3600));

        authService.login(new LoginRequisicaoDTO("ash@pokemon.com", "senha123"), "127.0.0.1");

        var captor = ArgumentCaptor.forClass(LogLogin.class);
        verify(logLoginRepository).save(captor.capture());
        assertThat(captor.getValue().getResultado()).isEqualTo("SUCESSO");
        assertThat(captor.getValue().getUsuarioId()).isEqualTo(usuarioAtivo.getId());
    }

    @Test
    void loginDeveNormalizarEmailParaMinusculas() {
        when(usuarioRepository.findByEmail("ash@pokemon.com")).thenReturn(Optional.of(usuarioAtivo));
        when(codificadorDeSenha.matches("senha123", "hashBcrypt")).thenReturn(true);
        when(jwtService.gerar(usuarioAtivo.getId())).thenReturn("jwt-token-gerado");
        when(jwtService.extrairExp("jwt-token-gerado")).thenReturn(Instant.now().plusSeconds(3600));

        authService.login(new LoginRequisicaoDTO("ASH@POKEMON.COM", "senha123"), "127.0.0.1");

        verify(usuarioRepository).findByEmail("ash@pokemon.com");
    }

    // T023 — US2: login com credenciais inválidas

    @Test
    void loginComEmailInexistenteDeveLancar401Generico() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequisicaoDTO("inexistente@pokemon.com", "senha"), "127.0.0.1"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("E-mail ou senha inválidos");

        verify(logLoginRepository).save(any(LogLogin.class));
    }

    @Test
    void loginComContaInativaDeveLancar401Generico() {
        Usuario usuarioInativo = new Usuario(UUID.randomUUID(), "Gary", "gary@pokemon.com", "hash", Instant.now(), false, 0, null);
        when(usuarioRepository.findByEmail("gary@pokemon.com")).thenReturn(Optional.of(usuarioInativo));

        assertThatThrownBy(() -> authService.login(new LoginRequisicaoDTO("gary@pokemon.com", "senha"), "127.0.0.1"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("E-mail ou senha inválidos");

        verify(logLoginRepository).save(any(LogLogin.class));
    }

    @Test
    void loginComSenhaErradaDeveLancar401Generico() {
        when(usuarioRepository.findByEmail("ash@pokemon.com")).thenReturn(Optional.of(usuarioAtivo));
        when(codificadorDeSenha.matches("senhaErrada", "hashBcrypt")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequisicaoDTO("ash@pokemon.com", "senhaErrada"), "127.0.0.1"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("E-mail ou senha inválidos");

        verify(logLoginRepository).save(any(LogLogin.class));
    }

    @Test
    void loginComSenhaErradaDeveSalvarLogFalha() {
        when(usuarioRepository.findByEmail("ash@pokemon.com")).thenReturn(Optional.of(usuarioAtivo));
        when(codificadorDeSenha.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequisicaoDTO("ash@pokemon.com", "errada"), "127.0.0.1"))
                .isInstanceOf(ResponseStatusException.class);

        var captor = ArgumentCaptor.forClass(LogLogin.class);
        verify(logLoginRepository).save(captor.capture());
        assertThat(captor.getValue().getResultado()).isEqualTo("FALHA");
    }

    // T026 — US3: bloqueio por tentativas excessivas

    @Test
    void loginComContaBloqueadaDeveLancar401Generico() {
        Usuario usuarioBloqueado = new Usuario(UUID.randomUUID(), "Ash", "ash@pokemon.com", "hash", Instant.now(), true, 5, Instant.now().plusSeconds(900));
        when(usuarioRepository.findByEmail("ash@pokemon.com")).thenReturn(Optional.of(usuarioBloqueado));

        assertThatThrownBy(() -> authService.login(new LoginRequisicaoDTO("ash@pokemon.com", "qualquersenha"), "127.0.0.1"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("E-mail ou senha inválidos");
    }

    @Test
    void falhasConsecutivasDevemIncrementarTentativasFalhas() {
        when(usuarioRepository.findByEmail("ash@pokemon.com")).thenReturn(Optional.of(usuarioAtivo));
        when(codificadorDeSenha.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequisicaoDTO("ash@pokemon.com", "errada"), "127.0.0.1"))
                .isInstanceOf(ResponseStatusException.class);

        var captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getTentativasFalhas()).isEqualTo(1);
    }

    @Test
    void quintaFalhaDeveDefinirBloqueadoAte() {
        Usuario usuarioQuatroPrevias = new Usuario(UUID.randomUUID(), "Ash", "ash@pokemon.com", "hash", Instant.now(), true, 4, null);
        when(usuarioRepository.findByEmail("ash@pokemon.com")).thenReturn(Optional.of(usuarioQuatroPrevias));
        when(codificadorDeSenha.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequisicaoDTO("ash@pokemon.com", "errada"), "127.0.0.1"))
                .isInstanceOf(ResponseStatusException.class);

        var captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getBloqueadoAte()).isNotNull();
        assertThat(captor.getValue().getBloqueadoAte()).isAfter(Instant.now());
    }

    @Test
    void loginSucessoDeveZerarTentativasFalhas() {
        Usuario usuarioComFalhas = new Usuario(UUID.randomUUID(), "Ash", "ash@pokemon.com", "hashBcrypt", Instant.now(), true, 3, null);
        when(usuarioRepository.findByEmail("ash@pokemon.com")).thenReturn(Optional.of(usuarioComFalhas));
        when(codificadorDeSenha.matches("certa", "hashBcrypt")).thenReturn(true);
        when(jwtService.gerar(usuarioComFalhas.getId())).thenReturn("token");
        when(jwtService.extrairExp("token")).thenReturn(Instant.now().plusSeconds(3600));

        authService.login(new LoginRequisicaoDTO("ash@pokemon.com", "certa"), "127.0.0.1");

        var captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getTentativasFalhas()).isZero();
        assertThat(captor.getValue().getBloqueadoAte()).isNull();
    }

    // T028 — US4: logout e cenário multi-dispositivo

    @Test
    void logoutDeveInserirJtiNaDenylist() {
        String token = "jwt-valido";
        String jtiStr = UUID.randomUUID().toString();
        Instant expiraEm = Instant.now().plusSeconds(3600);
        when(jwtService.extrairJti(token)).thenReturn(jtiStr);
        when(jwtService.extrairExp(token)).thenReturn(expiraEm);

        authService.logout(token);

        var captor = ArgumentCaptor.forClass(TokenRevogado.class);
        verify(tokenRevogadoRepository).save(captor.capture());
        assertThat(captor.getValue().getJti()).isEqualTo(UUID.fromString(jtiStr));
        assertThat(captor.getValue().getExpiraEm()).isEqualTo(expiraEm);
    }

    @Test
    void logoutDeTokenANaoDeveAfetarTokenB() {
        String tokenA = "token-a";
        String tokenB = "token-b";
        UUID jtiA = UUID.randomUUID();
        UUID jtiB = UUID.randomUUID();
        Instant exp = Instant.now().plusSeconds(3600);

        when(jwtService.extrairJti(tokenA)).thenReturn(jtiA.toString());
        when(jwtService.extrairExp(tokenA)).thenReturn(exp);

        authService.logout(tokenA);

        var captor = ArgumentCaptor.forClass(TokenRevogado.class);
        verify(tokenRevogadoRepository).save(captor.capture());
        assertThat(captor.getValue().getJti()).isEqualTo(jtiA);
        assertThat(captor.getValue().getJti()).isNotEqualTo(jtiB);
    }
}
