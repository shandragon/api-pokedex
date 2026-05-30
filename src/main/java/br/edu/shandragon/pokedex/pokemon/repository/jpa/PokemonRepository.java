package br.edu.shandragon.pokedex.pokemon.repository.jpa;

import br.edu.shandragon.pokedex.pokemon.entity.Pokemon;
import br.edu.shandragon.pokedex.pokemon.entity.Tipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PokemonRepository extends JpaRepository<Pokemon, UUID> {

    boolean existsByNumeroPokdex(Integer numeroPokdex);

    Optional<Pokemon> findByNome(String nome);

    List<Pokemon> findAllByTiposContaining(Tipo tipo);
}
