package br.edu.shandragon.pokedex.service;

import br.edu.shandragon.pokedex.dto.EvolucaoDTO;
import br.edu.shandragon.pokedex.dto.PokemonDTO;
import br.edu.shandragon.pokedex.dto.TipoDTO;
import br.edu.shandragon.pokedex.model.Evolucao;
import br.edu.shandragon.pokedex.model.Pokemon;
import br.edu.shandragon.pokedex.repository.EvolucaoRepository;
import br.edu.shandragon.pokedex.repository.PokemonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PokemonService {

    @Autowired
    private PokemonRepository pokemonRepository;

    @Autowired
    private EvolucaoRepository evolucaoRepository;

    public Map<String, List<PokemonDTO>> listarAgrupadoPorTipo() {
        List<Pokemon> todos = pokemonRepository.findAll();
        return todos.stream()
                .flatMap(p -> p.getTipos().stream()
                        .map(tipo -> Map.entry(tipo.getNome(), toPokemonDTO(p))))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
    }

    public List<EvolucaoDTO> buscarEvolucoes(Long pokemonId) {
        return evolucaoRepository.findByPokemonOrigemIdOrPokemonDestinoId(pokemonId, pokemonId)
                .stream()
                .map(this::toEvolucaoDTO)
                .collect(Collectors.toList());
    }

    private PokemonDTO toPokemonDTO(Pokemon pokemon) {
        Set<TipoDTO> tiposDTO = pokemon.getTipos().stream()
                .map(t -> new TipoDTO(t.getId(), t.getNome()))
                .collect(Collectors.toSet());
        return new PokemonDTO(pokemon.getId(), pokemon.getNome(), tiposDTO);
    }

    private PokemonDTO toPokemonResumoDTO(Pokemon pokemon) {
        return new PokemonDTO(pokemon.getId(), pokemon.getNome(), Set.of());
    }

    private EvolucaoDTO toEvolucaoDTO(Evolucao evolucao) {
        return new EvolucaoDTO(
                evolucao.getId(),
                toPokemonResumoDTO(evolucao.getPokemonOrigem()),
                toPokemonResumoDTO(evolucao.getPokemonDestino())
        );
    }
}
