package br.edu.shandragon.pokedex.pokemon.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record PokemonRequisicaoDTO(
        @NotNull @Min(1) Integer numeroPokdex,
        @NotBlank String nome,
        List<String> tipos,
        List<Map<String, Object>> evolucoes,
        Map<String, Object> atributosExtras
) {}
