package br.edu.shandragon.pokedex.pokemon.documento;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document(collection = "pokemon_atributos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PokemonAtributos {

    @Id
    private String id;

    private Map<String, Object> atributos = new HashMap<>();
}
