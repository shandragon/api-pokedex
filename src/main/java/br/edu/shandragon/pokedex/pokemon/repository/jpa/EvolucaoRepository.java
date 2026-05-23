package br.edu.shandragon.pokedex.pokemon.repository.jpa;

import br.edu.shandragon.pokedex.pokemon.entity.Evolucao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EvolucaoRepository extends JpaRepository<Evolucao, UUID> {

    List<Evolucao> findByPokemonOrigemIdOrPokemonDestinoId(UUID origemId, UUID destinoId);
}
