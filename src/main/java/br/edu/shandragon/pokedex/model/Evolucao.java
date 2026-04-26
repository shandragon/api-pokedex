package br.edu.shandragon.pokedex.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "evolucoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Evolucao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pokemon_origem_id")
    private Pokemon pokemonOrigem;

    @ManyToOne
    @JoinColumn(name = "pokemon_destino_id")
    private Pokemon pokemonDestino;
}
