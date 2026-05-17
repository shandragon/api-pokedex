package br.edu.shandragon.pokedex.pokemon.repositorio.mongo;

import br.edu.shandragon.pokedex.pokemon.documento.PokemonAtributos;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PokemonAtributosRepositorio extends MongoRepository<PokemonAtributos, String> {
}
