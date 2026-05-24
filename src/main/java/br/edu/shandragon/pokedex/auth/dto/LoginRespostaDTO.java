package br.edu.shandragon.pokedex.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record LoginRespostaDTO(
        String token,
        @JsonProperty("expira_em") Instant expiraEm
) {
}
