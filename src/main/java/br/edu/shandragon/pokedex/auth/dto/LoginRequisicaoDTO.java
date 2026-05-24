package br.edu.shandragon.pokedex.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequisicaoDTO(
        @NotBlank @Email String email,
        @NotBlank String senha
) {
}
