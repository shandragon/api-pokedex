package br.edu.shandragon.pokedex.usuario.servico;

import br.edu.shandragon.pokedex.compartilhado.UuidUtil;
import br.edu.shandragon.pokedex.usuario.dto.UsuarioRequisicaoDTO;
import br.edu.shandragon.pokedex.usuario.dto.UsuarioRespostaDTO;
import br.edu.shandragon.pokedex.usuario.dto.UsuarioRespostaPublicaDTO;
import br.edu.shandragon.pokedex.usuario.entidade.Usuario;
import br.edu.shandragon.pokedex.usuario.repositorio.jpa.UsuarioRepositorio;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UsuarioServico {

    private final UsuarioRepositorio repositorio;
    private final PasswordEncoder codificadorDeSenha;

    public UsuarioServico(UsuarioRepositorio repositorio, PasswordEncoder codificadorDeSenha) {
        this.repositorio = repositorio;
        this.codificadorDeSenha = codificadorDeSenha;
    }

    @Transactional
    public UsuarioRespostaDTO cadastrar(UsuarioRequisicaoDTO dto) {
        if (repositorio.existsByEmail(dto.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "E-mail já cadastrado: " + dto.email());
        }
        var usuario = new Usuario(
                UuidUtil.gerarV7(),
                dto.nome(),
                dto.email(),
                codificadorDeSenha.encode(dto.senha()),
                Instant.now()
        );
        var salvo = repositorio.save(usuario);
        return new UsuarioRespostaDTO(salvo.getId(), salvo.getNome(), salvo.getEmail(), salvo.getCriadoEm());
    }

    public List<UsuarioRespostaPublicaDTO> listar() {
        return repositorio.findAll().stream()
                .map(u -> new UsuarioRespostaPublicaDTO(u.getId(), u.getNome()))
                .toList();
    }

    public UsuarioRespostaPublicaDTO buscarPorId(UUID id) {
        var usuario = repositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuário não encontrado: " + id));
        return new UsuarioRespostaPublicaDTO(usuario.getId(), usuario.getNome());
    }
}
