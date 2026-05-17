package br.edu.shandragon.pokedex.usuario.repositorio.jpa;

import br.edu.shandragon.pokedex.usuario.entidade.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, UUID> {

    boolean existsByEmail(String email);
}
