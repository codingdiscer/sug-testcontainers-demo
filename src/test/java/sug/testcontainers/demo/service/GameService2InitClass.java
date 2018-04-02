package sug.testcontainers.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import sug.testcontainers.demo.DemoApplication;
import sug.testcontainers.demo.model.Game;
import sug.testcontainers.demo.model.GameComplexity;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Performs an integration test starting with an empty postgres instance, and prepare
 * the schema and populate the table programmatically
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = GameService2InitClass.Initializer.class, classes = { DemoApplication.class })
@Slf4j
public class GameService2InitClass {

    // initializes the testcontainer of an empty postgres instance, and kick of an init method
    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=jdbc:tc:postgresql://hostname/databasename?" +
                            "TC_INITFUNCTION=sug.testcontainers.demo.service.GameService2InitClass::initGameDb",
                    "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
                    "spring.jmx.default-domain=app" + new Random().nextInt())
                    .applyTo(applicationContext);
        }
    }

    public static void initGameDb(Connection connection) throws SQLException {
        connection.prepareStatement("CREATE TABLE game (game_id serial PRIMARY KEY, game_name text, complexity integer)").execute();
        connection.prepareStatement("insert into game (game_name, complexity) values ('Sorry', 0)").execute();
        connection.prepareStatement("insert into game (game_name, complexity) values ('King Of New York', 1)").execute();
        connection.prepareStatement("insert into game (game_name, complexity) values ('Mage Knight', 2)").execute();
    }


    // the class under test
    @Autowired GameService gameService;

    // interesting to poke a stick at
    @Autowired DataSource dataSource;


    @Test
    public void testGameServiceOperations() throws Exception {
        // should be 3 games
        assertEquals(gameService.getAllGames().size(), 3);

        // save a new game...make sure the id gets auto-assigned
        Game game = gameService.saveGame(new Game(null, "Cards Against Humanity", GameComplexity.SIMPLE));
        assertNotNull(game.getGameId());

        // should be 4 games now
        assertEquals(gameService.getAllGames().size(), 4);


        log.info("testGameServiceOperations() :: dataSource.connection.metaData.url=" + dataSource.getConnection().getMetaData().getURL());

        // sleep to allow time to check the db
        //Thread.sleep(240000);
    }

}
