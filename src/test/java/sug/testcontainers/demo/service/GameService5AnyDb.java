package sug.testcontainers.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import sug.testcontainers.demo.DemoApplication;
import sug.testcontainers.demo.model.Game;
import sug.testcontainers.demo.model.GameComplexity;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Simple test case that lets the GameServiceTestHelper prepare the data source
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = GameServiceTestHelper.Initializer.class, classes = { DemoApplication.class })
@Slf4j
public class GameService5AnyDb {

    // the class under test
    @Autowired GameService gameService;

    // something else useful to look at
    @Autowired DataSource dataSource;


    @Test
    public void testGameServiceOperations() throws Exception {
        // track how many games are in the db
        int preAddGameCount = gameService.getAllGames().size();

        // save a new game...make sure the id gets auto-assigned
        Game game = gameService.saveGame(new Game(null, "Cards Against Humanity", GameComplexity.SIMPLE));
        assertNotNull(game.getGameId());

        // should be +1 now
        assertEquals(gameService.getAllGames().size(), preAddGameCount + 1);


        log.info("testGameServiceOperations() :: dataSource.connection.metaData.url=" + dataSource.getConnection().getMetaData().getURL());

        // sleep to allow time to check the db
        //Thread.sleep(240000);
    }

}
