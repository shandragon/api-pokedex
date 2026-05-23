package br.edu.shandragon.pokedex.pokemon.repository.jpa;

import br.edu.shandragon.pokedex.pokemon.entity.Tipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TipoRepository extends JpaRepository<Tipo, UUID> {

    Optional<Tipo> findByNome(String nome);

    boolean existsByNome(String nome);
}
