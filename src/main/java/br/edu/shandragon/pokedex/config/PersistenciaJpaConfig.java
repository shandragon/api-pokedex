package br.edu.shandragon.pokedex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
        "br.edu.shandragon.pokedex.pokemon.repository.jpa",
        "br.edu.shandragon.pokedex.usuario.repository.jpa",
        "br.edu.shandragon.pokedex.auth.repository.jpa"
})
public class PersistenciaJpaConfig {
}
