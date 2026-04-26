package br.edu.shandragon.pokedex.service;

import br.edu.shandragon.pokedex.model.Pokemon;
import br.edu.shandragon.pokedex.model.Evolucao;
import br.edu.shandragon.pokedex.repository.PokemonRepository;
import br.edu.shandragon.pokedex.repository.EvolucaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PokemonService {

    @Autowired
    private PokemonRepository pokemonRepository;

    @Autowired
    private EvolucaoRepository evolucaoRepository;

    public Map<String, List<Pokemon>> listarAgrupadoPorTipo() {
        List<Pokemon> todos = pokemonRepository.findAll();
        return todos.stream()
                .flatMap(p -> p.getTipos().stream()
                        .map(tipo -> Map.entry(tipo.getNome(), p)))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
    }

    public List<Evolucao> buscarEvolucoes(Long pokemonId) {
        return evolucaoRepository.findByPokemonOrigemIdOrPokemonDestinoId(pokemonId, pokemonId);
    }
}
