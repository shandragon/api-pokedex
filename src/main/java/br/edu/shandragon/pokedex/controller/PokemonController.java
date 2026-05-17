package br.edu.shandragon.pokedex.controller;

import br.edu.shandragon.pokedex.dto.EvolucaoDTO;
import br.edu.shandragon.pokedex.dto.PokemonDTO;
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
@RequestMapping("/api/pokedex")
public class PokemonController {

    @Autowired
    private PokemonService pokemonService;

    @GetMapping("/por-tipo")
    public ResponseEntity<Map<String, List<PokemonDTO>>> listarPorTipo() {
        return ResponseEntity.ok(pokemonService.listarAgrupadoPorTipo());
    }

    @GetMapping("/{id}/evolucoes")
    public ResponseEntity<List<EvolucaoDTO>> listarEvolucoes(@PathVariable Long id) {
        return ResponseEntity.ok(pokemonService.buscarEvolucoes(id));
    }
}
