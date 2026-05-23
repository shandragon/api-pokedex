package br.edu.shandragon.pokedex.pokemon.controller;

import br.edu.shandragon.pokedex.pokemon.dto.PokemonRequisicaoDTO;
import br.edu.shandragon.pokedex.pokemon.dto.PokemonRespostaDTO;
import br.edu.shandragon.pokedex.pokemon.service.PokemonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class PokemonController {

    private final PokemonService servico;

    public PokemonController(PokemonService servico) {
        this.servico = servico;
    }

    @PostMapping("/api/pokemon")
    public ResponseEntity<PokemonRespostaDTO> criar(@Valid @RequestBody PokemonRequisicaoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servico.criar(dto));
    }

    @GetMapping("/api/pokemon")
    public ResponseEntity<List<PokemonRespostaDTO>> listar() {
        return ResponseEntity.ok(servico.listarTodos());
    }

    @GetMapping("/api/pokemon/{id}")
    public ResponseEntity<PokemonRespostaDTO> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(servico.buscarPorId(id));
    }

    @GetMapping("/api/pokedex/por-tipo")
    public ResponseEntity<Map<String, List<PokemonRespostaDTO>>> listarPorTipo() {
        return ResponseEntity.ok(servico.listarAgrupadoPorTipo());
    }

    @GetMapping("/api/pokedex/{id}/evolucoes")
    public ResponseEntity<List<Map<String, Object>>> listarEvolucoes(@PathVariable String id) {
        return ResponseEntity.ok(servico.buscarEvolucoes(id));
    }
}
