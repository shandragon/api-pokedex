package br.edu.shandragon.pokedex.pokemon.repositorio.jpa;

import br.edu.shandragon.pokedex.pokemon.entidade.Pokemon;
import br.edu.shandragon.pokedex.pokemon.entidade.Tipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PokemonRepositorio extends JpaRepository<Pokemon, UUID> {

    boolean existsByNumeroPokdex(Integer numeroPokdex);

    List<Pokemon> findAllByTiposContaining(Tipo tipo);
}
