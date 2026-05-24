package br.edu.shandragon.pokedex.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tokens_revogados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenRevogado {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID jti;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;
}
