package br.edu.shandragon.pokedex.repository;

import br.edu.shandragon.pokedex.model.Evolucao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EvolucaoRepository extends JpaRepository<Evolucao, Long> {
    List<Evolucao> findByPokemonOrigemIdOrPokemonDestinoId(Long pokemonOrigemId, Long pokemonDestinoId);
}
