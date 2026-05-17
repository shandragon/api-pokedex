package br.edu.shandragon.pokedex.usuario.dto;

import java.time.Instant;
import java.util.UUID;

public record UsuarioRespostaDTO(
        UUID id,
        String nome,
        String email,
        Instant criadoEm
) {}
