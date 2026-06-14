package br.edu.shandragon.pokedex.pokemon.service;

import br.edu.shandragon.pokedex.compartilhado.dto.PaginaRespostaDTO;
import br.edu.shandragon.pokedex.pokemon.document.PokemonAtributos;
import br.edu.shandragon.pokedex.pokemon.dto.PokemonRequisicaoDTO;
import br.edu.shandragon.pokedex.pokemon.entity.Evolucao;
import br.edu.shandragon.pokedex.pokemon.entity.Pokemon;
import br.edu.shandragon.pokedex.pokemon.entity.Tipo;
import br.edu.shandragon.pokedex.pokemon.repository.jpa.EvolucaoRepository;
import br.edu.shandragon.pokedex.pokemon.repository.jpa.PokemonRepository;
import br.edu.shandragon.pokedex.pokemon.repository.jpa.TipoRepository;
import br.edu.shandragon.pokedex.pokemon.repository.mongo.PokemonAtributosRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PokemonServiceTest {

    @Mock private PokemonRepository pokemonRepositorio;
    @Mock private TipoRepository tipoRepositorio;
    @Mock private EvolucaoRepository evolucaoRepositorio;
    @Mock private PokemonAtributosRepository atributosRepositorio;

    @InjectMocks
    private PokemonService servico;

    private Tipo tipoPlanta;
    private Pokemon pokemonSalvo;

    @BeforeEach
    void setUp() {
        tipoPlanta = new Tipo(UUID.randomUUID(), "Planta");
        pokemonSalvo = new Pokemon(UUID.randomUUID(), 1, "Bulbasaur", Set.of(tipoPlanta));
        lenient().when(evolucaoRepositorio.findByPokemonOrigemIdOrPokemonDestinoId(any(), any()))
                .thenReturn(List.of());
    }

    @Test
    void deveCriarPokemonComAtributosFixosEFlexiveis() {
        when(pokemonRepositorio.existsByNumeroPokdex(1)).thenReturn(false);
        when(tipoRepositorio.findByNome("Planta")).thenReturn(Optional.of(tipoPlanta));
        when(pokemonRepositorio.save(any(Pokemon.class))).thenReturn(pokemonSalvo);
        when(atributosRepositorio.save(any(PokemonAtributos.class))).thenAnswer(i -> i.getArgument(0));

        var dto = new PokemonRequisicaoDTO(1, "Bulbasaur", List.of("Planta"), List.of(),
                Map.of("ataques", List.of("Absorver")));
        var resposta = servico.criar(dto);

        assertThat(resposta.nome()).isEqualTo("Bulbasaur");
        assertThat(resposta.numeroPokdex()).isEqualTo(1);
        verify(pokemonRepositorio).save(any(Pokemon.class));
        verify(atributosRepositorio).save(any(PokemonAtributos.class));
    }

    @Test
    void deveLancarConflictParaNumeroPokdexDuplicado() {
        when(pokemonRepositorio.existsByNumeroPokdex(1)).thenReturn(true);

        var dto = new PokemonRequisicaoDTO(1, "Bulbasaur", List.of(), List.of(), Map.of());
        assertThatThrownBy(() -> servico.criar(dto))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deveLancarBadRequestParaTipoInvalido() {
        when(pokemonRepositorio.existsByNumeroPokdex(1)).thenReturn(false);
        when(tipoRepositorio.findByNome("TipoInvalido")).thenReturn(Optional.empty());

        var dto = new PokemonRequisicaoDTO(1, "Bulbasaur", List.of("TipoInvalido"), List.of(), Map.of());
        assertThatThrownBy(() -> servico.criar(dto))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deveListarTodosComMergeDeAtributos() {
        when(pokemonRepositorio.findAll()).thenReturn(List.of(pokemonSalvo));
        var atributos = new PokemonAtributos(pokemonSalvo.getId().toString(),
                Map.of("ataques", List.of("Absorver")));
        when(atributosRepositorio.findById(pokemonSalvo.getId().toString()))
                .thenReturn(Optional.of(atributos));

        var resposta = servico.listar(null, null);

        assertThat(resposta.itens()).hasSize(1);
        assertThat(resposta.itens().get(0).atributosExtras()).containsKey("ataques");
        assertThat(resposta.totalItens()).isEqualTo(1);
    }

    @Test
    void deveListarPaginado() {
        Page<Pokemon> page = new PageImpl<>(List.of(pokemonSalvo));
        when(pokemonRepositorio.findAll(any(PageRequest.class))).thenReturn(page);
        when(atributosRepositorio.findById(anyString())).thenReturn(Optional.empty());

        var resposta = servico.listar(0, 1);

        assertThat(resposta.itens()).hasSize(1);
        assertThat(resposta.totalItens()).isEqualTo(1);
        assertThat(resposta.itensPorPagina()).isEqualTo(1);
        assertThat(resposta.paginaAtual()).isEqualTo(0);
        verify(pokemonRepositorio).findAll(PageRequest.of(0, 1));
    }

    @Test
    void deveListarSemAtributosFlexiveisQuandoMongoNaoTemDocumento() {
        when(pokemonRepositorio.findAll()).thenReturn(List.of(pokemonSalvo));
        when(atributosRepositorio.findById(pokemonSalvo.getId().toString()))
                .thenReturn(Optional.empty());

        var resposta = servico.listar(null, null);

        assertThat(resposta.itens()).hasSize(1);
        assertThat(resposta.itens().get(0).atributosExtras()).isEmpty();
    }

    @Test
    void deveBuscarPorIdComMerge() {
        when(pokemonRepositorio.findById(pokemonSalvo.getId())).thenReturn(Optional.of(pokemonSalvo));
        when(atributosRepositorio.findById(pokemonSalvo.getId().toString()))
                .thenReturn(Optional.of(new PokemonAtributos(pokemonSalvo.getId().toString(),
                        Map.of("fraquezas", List.of("Fogo")))));

        var resultado = servico.buscarPorId(pokemonSalvo.getId().toString());

        assertThat(resultado.nome()).isEqualTo("Bulbasaur");
        assertThat(resultado.atributosExtras()).containsKey("fraquezas");
    }

    @Test
    void deveLancarNotFoundParaIdInexistente() {
        when(pokemonRepositorio.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servico.buscarPorId(UUID.randomUUID().toString()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deveCriarPokemonComEvolucaoValida() {
        var destino = new Pokemon(UUID.randomUUID(), 2, "Ivysaur", Set.of(tipoPlanta));
        var evolucaoDTO = Map.<String, Object>of(
                "idPokemonOrigem", pokemonSalvo.getId().toString(),
                "idPokemonDestino", destino.getId().toString()
        );

        when(pokemonRepositorio.existsByNumeroPokdex(1)).thenReturn(false);
        when(tipoRepositorio.findByNome("Planta")).thenReturn(Optional.of(tipoPlanta));
        when(pokemonRepositorio.save(any(Pokemon.class))).thenReturn(pokemonSalvo);
        when(pokemonRepositorio.findById(pokemonSalvo.getId())).thenReturn(Optional.of(pokemonSalvo));
        when(pokemonRepositorio.findById(destino.getId())).thenReturn(Optional.of(destino));

        var dto = new PokemonRequisicaoDTO(1, "Bulbasaur", List.of("Planta"), List.of(evolucaoDTO), Map.of());
        servico.criar(dto);

        verify(evolucaoRepositorio).save(any(Evolucao.class));
    }

    @Test
    void deveCriarPokemonIgnorandoEvolucaoComReferenciasInvalidas() {
        var evolucaoInvalida = Map.<String, Object>of(
                "idPokemonOrigem", UUID.randomUUID().toString(),
                "idPokemonDestino", UUID.randomUUID().toString()
        );

        when(pokemonRepositorio.existsByNumeroPokdex(1)).thenReturn(false);
        when(tipoRepositorio.findByNome("Planta")).thenReturn(Optional.of(tipoPlanta));
        when(pokemonRepositorio.save(any(Pokemon.class))).thenReturn(pokemonSalvo);
        when(pokemonRepositorio.findById(any(UUID.class))).thenReturn(Optional.empty());

        var dto = new PokemonRequisicaoDTO(1, "Bulbasaur", List.of("Planta"), List.of(evolucaoInvalida), Map.of());
        servico.criar(dto);

        verify(evolucaoRepositorio, never()).save(any());
    }
}
