package br.edu.shandragon.pokedex.pokemon.dto;

import java.util.List;
import java.util.Map;

public record PokemonRespostaDTO(
        String id,
        Integer numeroPokdex,
        String nome,
        List<String> tipos,
        List<Map<String, Object>> evolucoes,
        Map<String, Object> atributosExtras
) {}
