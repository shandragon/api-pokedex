package br.edu.shandragon.pokedex.auth.repository.jpa;

import br.edu.shandragon.pokedex.auth.entity.TokenRevogado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TokenRevogadoRepository extends JpaRepository<TokenRevogado, UUID> {

    boolean existsByJti(UUID jti);
}
