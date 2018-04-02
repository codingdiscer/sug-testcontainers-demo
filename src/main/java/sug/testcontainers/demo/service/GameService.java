package sug.testcontainers.demo.service;

import org.springframework.stereotype.Service;
import sug.testcontainers.demo.dao.GameRepository;
import sug.testcontainers.demo.model.Game;

import java.util.List;

@Service
public class GameService {

    private GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    Game saveGame(Game game) {
        return gameRepository.save(game);
    }

    List<Game> getAllGames() {
        return (List<Game>)gameRepository.findAll();
    }
}
