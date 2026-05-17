package br.edu.shandragon.pokedex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
        "br.edu.shandragon.pokedex.pokemon.repositorio.jpa",
        "br.edu.shandragon.pokedex.usuario.repositorio.jpa"
})
public class PersistenciaJpaConfig {
}
