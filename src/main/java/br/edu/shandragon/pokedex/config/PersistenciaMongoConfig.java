package br.edu.shandragon.pokedex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "br.edu.shandragon.pokedex.pokemon.repositorio.mongo")
public class PersistenciaMongoConfig {
}
