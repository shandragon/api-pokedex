package br.edu.shandragon.pokedex.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class PokemonDTO {
    private Long id;
    private String nome;
    private Set<TipoDTO> tipos;
}
