package br.edu.shandragon.pokedex.pokemon.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "pokemons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pokemon {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "numero_pokdex", nullable = false, unique = true)
    private Integer numeroPokdex;

    @Column(nullable = false)
    private String nome;

    @ManyToMany
    @JoinTable(
            name = "pokemon_tipos",
            joinColumns = @JoinColumn(name = "pokemon_id"),
            inverseJoinColumns = @JoinColumn(name = "tipo_id")
    )
    private Set<Tipo> tipos = new HashSet<>();
}
