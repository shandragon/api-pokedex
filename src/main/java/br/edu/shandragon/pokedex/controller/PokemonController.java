package br.edu.shandragon.pokedex.controller;

import br.edu.shandragon.pokedex.model.Pokemon;
import br.edu.shandragon.pokedex.model.Evolucao;
import br.edu.shandragon.pokedex.service.PokemonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pokemon")
public class PokemonController {

    @Autowired
    private PokemonService pokemonService;

    @GetMapping("/por-tipo")
    public ResponseEntity<Map<String, List<Pokemon>>> listarPorTipo() {
        return ResponseEntity.ok(pokemonService.listarAgrupadoPorTipo());
    }

    @GetMapping("/{id}/evolucoes")
    public ResponseEntity<List<Evolucao>> listarEvolucoes(@PathVariable Long id) {
        return ResponseEntity.ok(pokemonService.buscarEvolucoes(id));
    }
}
