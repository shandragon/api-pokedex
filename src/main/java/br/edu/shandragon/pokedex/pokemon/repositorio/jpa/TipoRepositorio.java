package br.edu.shandragon.pokedex.pokemon.repositorio.jpa;

import br.edu.shandragon.pokedex.pokemon.entidade.Tipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TipoRepositorio extends JpaRepository<Tipo, UUID> {

    Optional<Tipo> findByNome(String nome);

    boolean existsByNome(String nome);
}
