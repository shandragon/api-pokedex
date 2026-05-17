package br.edu.shandragon.pokedex.pokemon.entidade;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "evolucoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Evolucao {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pokemon_origem_id", nullable = false)
    private Pokemon pokemonOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pokemon_destino_id", nullable = false)
    private Pokemon pokemonDestino;
}
