package br.edu.shandragon.pokedex.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EvolucaoDTO {
    private Long id;
    private PokemonDTO pokemonOrigem;
    private PokemonDTO pokemonDestino;
}
