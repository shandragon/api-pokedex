package br.edu.shandragon.pokedex.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "pokemon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pokemon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @ManyToMany
    @JoinTable(
        name = "pokemon_tipo",
        joinColumns = @JoinColumn(name = "pokemon_id"),
        inverseJoinColumns = @JoinColumn(name = "tipo_id")
    )
    private Set<Tipo> tipos;
}
