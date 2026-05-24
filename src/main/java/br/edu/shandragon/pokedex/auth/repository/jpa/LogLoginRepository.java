package br.edu.shandragon.pokedex.auth.repository.jpa;

import br.edu.shandragon.pokedex.auth.entity.LogLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LogLoginRepository extends JpaRepository<LogLogin, UUID> {
}
