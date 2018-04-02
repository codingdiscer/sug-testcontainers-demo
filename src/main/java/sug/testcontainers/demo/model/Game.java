package sug.testcontainers.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name="game")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Game {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id")
    Integer gameId;

    @Column(name = "game_name")
    String name;

    @Column GameComplexity complexity;
}
