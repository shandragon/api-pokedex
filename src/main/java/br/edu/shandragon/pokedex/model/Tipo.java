package br.edu.shandragon.pokedex.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;
}
