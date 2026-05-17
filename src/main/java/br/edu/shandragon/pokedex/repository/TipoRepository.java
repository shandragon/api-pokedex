package br.edu.shandragon.pokedex.repository;

import br.edu.shandragon.pokedex.model.Tipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoRepository extends JpaRepository<Tipo, Long> {
}
