package br.edu.shandragon.pokedex.usuario.controller;

import br.edu.shandragon.pokedex.usuario.dto.UsuarioRequisicaoDTO;
import br.edu.shandragon.pokedex.usuario.dto.UsuarioRespostaDTO;
import br.edu.shandragon.pokedex.usuario.dto.UsuarioRespostaPublicaDTO;
import br.edu.shandragon.pokedex.usuario.servico.UsuarioServico;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioServico servico;

    public UsuarioController(UsuarioServico servico) {
        this.servico = servico;
    }

    @PostMapping
    public ResponseEntity<UsuarioRespostaDTO> cadastrar(@Valid @RequestBody UsuarioRequisicaoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servico.cadastrar(dto));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioRespostaPublicaDTO>> listar() {
        return ResponseEntity.ok(servico.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioRespostaPublicaDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(servico.buscarPorId(id));
    }
}
