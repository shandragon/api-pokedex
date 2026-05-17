package br.edu.shandragon.pokedex.exception;

public class PokemonNaoEncontradoException extends RuntimeException {
    public PokemonNaoEncontradoException(Long id) {
        super("Pokémon não encontrado: " + id);
    }
}
