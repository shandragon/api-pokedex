package br.edu.shandragon.pokedex.auth.service;

import br.edu.shandragon.pokedex.auth.dto.LoginRequisicaoDTO;
import br.edu.shandragon.pokedex.auth.dto.LoginRespostaDTO;
import br.edu.shandragon.pokedex.auth.entity.LogLogin;
import br.edu.shandragon.pokedex.auth.entity.TokenRevogado;
import br.edu.shandragon.pokedex.auth.repository.jpa.LogLoginRepository;
import br.edu.shandragon.pokedex.auth.repository.jpa.TokenRevogadoRepository;
import br.edu.shandragon.pokedex.usuario.repository.jpa.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private static final String MENSAGEM_CREDENCIAIS_INVALIDAS = "E-mail ou senha inválidos";

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder codificadorDeSenha;
    private final LogLoginRepository logLoginRepository;
    private final TokenRevogadoRepository tokenRevogadoRepository;
    private final int bloqueioTentativas;
    private final long bloqueioDuracaoMinutos;

    public AuthService(
            UsuarioRepository usuarioRepository,
            JwtService jwtService,
            PasswordEncoder codificadorDeSenha,
            LogLoginRepository logLoginRepository,
            TokenRevogadoRepository tokenRevogadoRepository,
            @Value("${app.seguranca.bloqueio-tentativas}") int bloqueioTentativas,
            @Value("${app.seguranca.bloqueio-duracao-minutos}") long bloqueioDuracaoMinutos) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.codificadorDeSenha = codificadorDeSenha;
        this.logLoginRepository = logLoginRepository;
        this.tokenRevogadoRepository = tokenRevogadoRepository;
        this.bloqueioTentativas = bloqueioTentativas;
        this.bloqueioDuracaoMinutos = bloqueioDuracaoMinutos;
    }

    public LoginRespostaDTO login(LoginRequisicaoDTO dto, String ip) {
        String email = dto.email().toLowerCase();

        var usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> {
            logLoginRepository.save(new LogLogin(null, ip, "FALHA"));
            return new ResponseStatusException(HttpStatus.UNAUTHORIZED, MENSAGEM_CREDENCIAIS_INVALIDAS);
        });

        if (!usuario.isAtivo()) {
            logLoginRepository.save(new LogLogin(usuario.getId(), ip, "FALHA"));
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, MENSAGEM_CREDENCIAIS_INVALIDAS);
        }

        if (usuario.getBloqueadoAte() != null && usuario.getBloqueadoAte().isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, MENSAGEM_CREDENCIAIS_INVALIDAS);
        }

        if (!codificadorDeSenha.matches(dto.senha(), usuario.getSenhaHash())) {
            usuario.setTentativasFalhas(usuario.getTentativasFalhas() + 1);
            if (usuario.getTentativasFalhas() >= bloqueioTentativas) {
                usuario.setBloqueadoAte(Instant.now().plus(bloqueioDuracaoMinutos, ChronoUnit.MINUTES));
            }
            usuarioRepository.save(usuario);
            logLoginRepository.save(new LogLogin(usuario.getId(), ip, "FALHA"));
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, MENSAGEM_CREDENCIAIS_INVALIDAS);
        }

        usuario.setTentativasFalhas(0);
        usuario.setBloqueadoAte(null);
        usuarioRepository.save(usuario);

        String token = jwtService.gerar(usuario.getId());
        Instant expiraEm = jwtService.extrairExp(token);
        logLoginRepository.save(new LogLogin(usuario.getId(), ip, "SUCESSO"));

        return new LoginRespostaDTO(token, expiraEm);
    }

    public void logout(String token) {
        UUID jti = UUID.fromString(jwtService.extrairJti(token));
        Instant expiraEm = jwtService.extrairExp(token);
        tokenRevogadoRepository.save(new TokenRevogado(jti, expiraEm));
    }
}
