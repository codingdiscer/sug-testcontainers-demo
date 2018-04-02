package sug.testcontainers.demo.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import sug.testcontainers.demo.model.Game;

@Repository
public interface GameRepository extends CrudRepository<Game, Integer> { }
