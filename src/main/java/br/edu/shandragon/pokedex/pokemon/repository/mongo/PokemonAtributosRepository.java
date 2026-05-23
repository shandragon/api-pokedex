package br.edu.shandragon.pokedex.pokemon.repository.mongo;

import br.edu.shandragon.pokedex.pokemon.document.PokemonAtributos;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PokemonAtributosRepository extends MongoRepository<PokemonAtributos, String> {
}
