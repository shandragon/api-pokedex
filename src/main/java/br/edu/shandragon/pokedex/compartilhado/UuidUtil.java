package br.edu.shandragon.pokedex.compartilhado;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.UUID;

public final class UuidUtil {

    private UuidUtil() {}

    public static UUID gerarV7() {
        return UuidCreator.getTimeOrderedEpoch();
    }
}
