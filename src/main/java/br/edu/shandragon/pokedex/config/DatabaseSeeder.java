package br.edu.shandragon.pokedex.config;

import br.edu.shandragon.pokedex.pokemon.dto.PokemonRequisicaoDTO;
import br.edu.shandragon.pokedex.pokemon.entity.Evolucao;
import br.edu.shandragon.pokedex.pokemon.entity.Pokemon;
import br.edu.shandragon.pokedex.pokemon.repository.jpa.EvolucaoRepository;
import br.edu.shandragon.pokedex.pokemon.repository.jpa.PokemonRepository;
import br.edu.shandragon.pokedex.pokemon.service.PokemonService;
import br.edu.shandragon.pokedex.compartilhado.UuidUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final PokemonService pokemonService;
    private final PokemonRepository pokemonRepository;
    private final EvolucaoRepository evolucaoRepository;

    public DatabaseSeeder(PokemonService pokemonService, 
                          PokemonRepository pokemonRepository,
                          EvolucaoRepository evolucaoRepository) {
        this.pokemonService = pokemonService;
        this.pokemonRepository = pokemonRepository;
        this.evolucaoRepository = evolucaoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (pokemonRepository.count() > 0) {
            log.info("Banco de dados já contém Pokémon. Pulando seed da 1ª Geração.");
            return;
        }

        log.info("Iniciando seed dos 151 Pokémon da 1ª Geração...");

        Map<String, List<String>> evolucoesPendentes = new HashMap<>();
        ClassPathResource resource = new ClassPathResource("pokemon_gen1.csv");

        // Primeiro Passo: Criar todos os Pokémon e seus atributos
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine(); // Pular cabeçalho

            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", -1);
                if (data.length < 11) continue;

                String nome = data[1];
                
                // Mapear dados do CSV
                int numero = Integer.parseInt(data[0]);
                List<String> tipos = Arrays.asList(data[2].split(";"));
                
                Map<String, Object> extras = new HashMap<>();
                Map<String, Integer> stats = new HashMap<>();
                stats.put("hp", Integer.parseInt(data[3]));
                stats.put("ataque", Integer.parseInt(data[4]));
                stats.put("defesa", Integer.parseInt(data[5]));
                stats.put("especial", Integer.parseInt(data[6]));
                stats.put("velocidade", Integer.parseInt(data[7]));
                
                extras.put("estatisticas", stats);
                extras.put("ataques", Arrays.asList(data[8].split(";")));
                extras.put("fraquezas", Arrays.asList(data[9].split(";")));
                extras.put("geracao", "1ª Geração");

                // Guardar evoluções para o segundo passo
                if (!data[10].isEmpty()) {
                    evolucoesPendentes.put(nome, Arrays.asList(data[10].split(";")));
                }

                var dto = new PokemonRequisicaoDTO(numero, nome, tipos, List.of(), extras);
                pokemonService.salvarParaSeed(dto);
            }
        }

        // Segundo Passo: Vincular Evoluções (1 query em vez de ~160)
        log.info("Vinculando evoluções...");
        Map<String, Pokemon> pokemonPorNome = new HashMap<>();
        for (Pokemon p : pokemonRepository.findAll()) {
            pokemonPorNome.put(p.getNome(), p);
        }

        for (var entry : evolucoesPendentes.entrySet()) {
            Pokemon origem = pokemonPorNome.get(entry.getKey());
            if (origem == null) continue;
            for (String nomeDestino : entry.getValue()) {
                Pokemon destino = pokemonPorNome.get(nomeDestino);
                if (destino != null) {
                    evolucaoRepository.save(new Evolucao(UuidUtil.gerarV7(), origem, destino));
                }
            }
        }

        log.info("Seed da 1ª Geração finalizado com sucesso!");
    }
}
