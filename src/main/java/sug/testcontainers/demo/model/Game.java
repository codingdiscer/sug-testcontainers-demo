package sug.testcontainers.demo.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Game {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id")
    Integer gameId;


    @Column(name = "game_name")
    String name;


    @Column GameComplexity complixity;
}
