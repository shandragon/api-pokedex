package br.edu.shandragon.pokedex.usuario.service;

import br.edu.shandragon.pokedex.usuario.dto.UsuarioRequisicaoDTO;
import br.edu.shandragon.pokedex.usuario.entity.Usuario;
import br.edu.shandragon.pokedex.usuario.repository.jpa.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repositorio;

    @Mock
    private PasswordEncoder codificadorDeSenha;

    @InjectMocks
    private UsuarioService servico;

    private Usuario usuarioExemplo;

    @BeforeEach
    void setUp() {
        usuarioExemplo = new Usuario(UUID.randomUUID(), "Ash Ketchum", "ash@pokemon.com", "hashBcrypt", Instant.now(), true, 0, null);
    }

    @Test
    void deveCadastrarUsuarioERetornarDtoSemSenha() {
        when(repositorio.existsByEmail("ash@pokemon.com")).thenReturn(false);
        when(codificadorDeSenha.encode("senha123")).thenReturn("hashBcrypt");
        when(repositorio.save(any(Usuario.class))).thenReturn(usuarioExemplo);

        var dto = new UsuarioRequisicaoDTO("Ash Ketchum", "ash@pokemon.com", "senha123");
        var resposta = servico.cadastrar(dto);

        assertThat(resposta.id()).isNotNull();
        assertThat(resposta.nome()).isEqualTo("Ash Ketchum");
        assertThat(resposta.email()).isEqualTo("ash@pokemon.com");
        verify(repositorio).save(any(Usuario.class));
    }

    @Test
    void deveLancarExcecaoQuandoEmailDuplicado() {
        when(repositorio.existsByEmail("ash@pokemon.com")).thenReturn(true);

        var dto = new UsuarioRequisicaoDTO("Ash", "ash@pokemon.com", "senha");
        assertThatThrownBy(() -> servico.cadastrar(dto))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deveListarUsuariosRetornandoApenasIdENome() {
        when(repositorio.findAll()).thenReturn(List.of(usuarioExemplo));

        var lista = servico.listar();

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).nome()).isEqualTo("Ash Ketchum");
    }

    @Test
    void deveBuscarPorIdExistente() {
        when(repositorio.findById(usuarioExemplo.getId())).thenReturn(Optional.of(usuarioExemplo));

        var resultado = servico.buscarPorId(usuarioExemplo.getId());

        assertThat(resultado.nome()).isEqualTo("Ash Ketchum");
    }

    @Test
    void deveLancarNotFoundParaIdInexistente() {
        when(repositorio.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servico.buscarPorId(UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class);
    }
}
