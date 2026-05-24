package br.edu.shandragon.pokedex.auth.controller;

import br.edu.shandragon.pokedex.auth.dto.LoginRequisicaoDTO;
import br.edu.shandragon.pokedex.auth.dto.LoginRespostaDTO;
import br.edu.shandragon.pokedex.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginRespostaDTO> login(
            @Valid @RequestBody LoginRequisicaoDTO dto,
            HttpServletRequest request) {
        var resposta = authService.login(dto, request.getRemoteAddr());
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String token = (String) request.getAttribute("jwtAtual");
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }
}
