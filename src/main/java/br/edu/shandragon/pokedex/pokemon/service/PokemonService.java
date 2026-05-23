package br.edu.shandragon.pokedex.pokemon.service;

import br.edu.shandragon.pokedex.compartilhado.UuidUtil;
import br.edu.shandragon.pokedex.pokemon.document.PokemonAtributos;
import br.edu.shandragon.pokedex.pokemon.dto.PokemonRequisicaoDTO;
import br.edu.shandragon.pokedex.pokemon.dto.PokemonRespostaDTO;
import br.edu.shandragon.pokedex.pokemon.entity.Evolucao;
import br.edu.shandragon.pokedex.pokemon.entity.Pokemon;
import br.edu.shandragon.pokedex.pokemon.entity.Tipo;
import br.edu.shandragon.pokedex.pokemon.repository.jpa.EvolucaoRepository;
import br.edu.shandragon.pokedex.pokemon.repository.jpa.PokemonRepository;
import br.edu.shandragon.pokedex.pokemon.repository.jpa.TipoRepository;
import br.edu.shandragon.pokedex.pokemon.repository.mongo.PokemonAtributosRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PokemonService {

    private final PokemonRepository pokemonRepositorio;
    private final TipoRepository tipoRepositorio;
    private final EvolucaoRepository evolucaoRepositorio;
    private final PokemonAtributosRepository atributosRepositorio;

    public PokemonService(PokemonRepository pokemonRepositorio,
                          TipoRepository tipoRepositorio,
                          EvolucaoRepository evolucaoRepositorio,
                          PokemonAtributosRepository atributosRepositorio) {
        this.pokemonRepositorio = pokemonRepositorio;
        this.tipoRepositorio = tipoRepositorio;
        this.evolucaoRepositorio = evolucaoRepositorio;
        this.atributosRepositorio = atributosRepositorio;
    }

    @Transactional
    public PokemonRespostaDTO criar(PokemonRequisicaoDTO dto) {
        if (pokemonRepositorio.existsByNumeroPokdex(dto.numeroPokdex())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Número do Pokédex " + dto.numeroPokdex() + " já existe");
        }

        Set<Tipo> tipos = resolverTipos(dto.tipos());

        var id = UuidUtil.gerarV7();
        var pokemon = new Pokemon(id, dto.numeroPokdex(), dto.nome(), tipos);
        var salvo = pokemonRepositorio.save(pokemon);

        salvarEvolucoes(dto.evolucoes());
        salvarAtributosFlexiveis(id.toString(), dto.atributosExtras());

        return montar(salvo, dto.atributosExtras() != null ? dto.atributosExtras() : Map.of());
    }

    public List<PokemonRespostaDTO> listarTodos() {
        return pokemonRepositorio.findAll().stream()
                .map(p -> {
                    var extras = atributosRepositorio.findById(p.getId().toString())
                            .map(PokemonAtributos::getAtributos)
                            .orElse(Map.of());
                    return montar(p, extras);
                })
                .toList();
    }

    public PokemonRespostaDTO buscarPorId(String id) {
        var uuid = parseUuid(id);
        var pokemon = pokemonRepositorio.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pokémon não encontrado: " + id));
        var extras = atributosRepositorio.findById(id)
                .map(PokemonAtributos::getAtributos)
                .orElse(Map.of());
        return montar(pokemon, extras);
    }

    public Map<String, List<PokemonRespostaDTO>> listarAgrupadoPorTipo() {
        return tipoRepositorio.findAll().stream()
                .filter(t -> !pokemonRepositorio.findAllByTiposContaining(t).isEmpty())
                .collect(Collectors.toMap(
                        Tipo::getNome,
                        t -> pokemonRepositorio.findAllByTiposContaining(t).stream()
                                .map(p -> montar(p, Map.of()))
                                .toList()
                ));
    }

    public List<Map<String, Object>> buscarEvolucoes(String id) {
        var uuid = parseUuid(id);
        if (!pokemonRepositorio.existsById(uuid)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pokémon não encontrado: " + id);
        }
        return evolucaoRepositorio.findByPokemonOrigemIdOrPokemonDestinoId(uuid, uuid)
                .stream()
                .map(this::evolucaoParaMapa)
                .toList();
    }

    private Set<Tipo> resolverTipos(List<String> nomes) {
        if (nomes == null || nomes.isEmpty()) return Set.of();
        Set<Tipo> tipos = new HashSet<>();
        for (String nome : nomes) {
            var tipo = tipoRepositorio.findByNome(nome)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Tipo inválido: " + nome));
            tipos.add(tipo);
        }
        return tipos;
    }

    private void salvarEvolucoes(List<Map<String, Object>> evolucoes) {
        if (evolucoes == null || evolucoes.isEmpty()) return;
        for (var e : evolucoes) {
            try {
                var origemId = UUID.fromString((String) e.get("idPokemonOrigem"));
                var destinoId = UUID.fromString((String) e.get("idPokemonDestino"));
                var origem = pokemonRepositorio.findById(origemId).orElse(null);
                var destino = pokemonRepositorio.findById(destinoId).orElse(null);
                if (origem != null && destino != null) {
                    evolucaoRepositorio.save(new Evolucao(UuidUtil.gerarV7(), origem, destino));
                }
            } catch (Exception ignorada) {
                // referência inválida — evolução ignorada sem falhar a criação
            }
        }
    }

    private void salvarAtributosFlexiveis(String id, Map<String, Object> extras) {
        if (extras == null || extras.isEmpty()) return;
        try {
            atributosRepositorio.save(new PokemonAtributos(id, extras));
        } catch (Exception ignorada) {
            // falha parcial tolerada: Pokémon existe no Postgres sem atributos flexíveis
        }
    }

    private PokemonRespostaDTO montar(Pokemon pokemon, Map<String, Object> extras) {
        var tiposNomes = pokemon.getTipos().stream()
                .map(Tipo::getNome)
                .sorted()
                .toList();
        var evolucoes = evolucaoRepositorio
                .findByPokemonOrigemIdOrPokemonDestinoId(pokemon.getId(), pokemon.getId())
                .stream()
                .map(this::evolucaoParaMapa)
                .toList();
        return new PokemonRespostaDTO(
                pokemon.getId().toString(),
                pokemon.getNumeroPokdex(),
                pokemon.getNome(),
                tiposNomes,
                evolucoes,
                extras
        );
    }

    private Map<String, Object> evolucaoParaMapa(Evolucao e) {
        return Map.of(
                "idPokemonOrigem", e.getPokemonOrigem().getId().toString(),
                "nomePokemonOrigem", e.getPokemonOrigem().getNome(),
                "idPokemonDestino", e.getPokemonDestino().getId().toString(),
                "nomePokemonDestino", e.getPokemonDestino().getNome()
        );
    }

    private UUID parseUuid(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pokémon não encontrado: " + id);
        }
    }
}
