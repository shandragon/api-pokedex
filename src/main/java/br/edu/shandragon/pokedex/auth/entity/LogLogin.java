package br.edu.shandragon.pokedex.auth.entity;

import br.edu.shandragon.pokedex.compartilhado.UuidUtil;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "log_login")
@Getter
@Setter
@NoArgsConstructor
public class LogLogin {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "usuario_id", columnDefinition = "uuid")
    private UUID usuarioId;

    @Column(nullable = false, length = 45)
    private String ip;

    @Column(nullable = false, length = 10)
    private String resultado;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    public LogLogin(UUID usuarioId, String ip, String resultado) {
        this.id = UuidUtil.gerarV7();
        this.usuarioId = usuarioId;
        this.ip = ip;
        this.resultado = resultado;
        this.criadoEm = Instant.now();
    }
}
