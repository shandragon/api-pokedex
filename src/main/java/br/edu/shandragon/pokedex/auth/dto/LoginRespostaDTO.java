package br.edu.shandragon.pokedex.auth.dto;

import java.time.Instant;

public record LoginRespostaDTO(
        String token,
        Instant expiraEm
) {
}
