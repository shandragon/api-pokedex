package br.edu.shandragon.pokedex.pokemon.repositorio.jpa;

import br.edu.shandragon.pokedex.pokemon.entidade.Evolucao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EvolucaoRepositorio extends JpaRepository<Evolucao, UUID> {

    List<Evolucao> findByPokemonOrigemIdOrPokemonDestinoId(UUID origemId, UUID destinoId);
}
