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
import org.testcontainers.containers.PostgreSQLContainer;
import sug.testcontainers.demo.DemoApplication;
import sug.testcontainers.demo.config.DemoConfig;
import sug.testcontainers.demo.dao.GameRepository;
import sug.testcontainers.demo.model.Game;
import sug.testcontainers.demo.model.GameComplexity;

import javax.sql.DataSource;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Performs an integration test starting with an empty postgres instance, and run a script from the
 * file system to prepare the schema and populate the table
 */
@RunWith(SpringRunner.class)
//@ContextConfiguration(initializers = GameService1InitScript.Initializer.class, classes = { DemoApplication.class } )

// it is possible to not run the entire application for the test...you can specify just the classes you need
// with one caveat - you need to have @EnableAutoConfiguration on one of the @Configuration classes
@ContextConfiguration(initializers = GameService1InitScript.Initializer.class, classes = {
        DemoConfig.class, GameRepository.class, GameService.class } )
@Slf4j
public class GameService1InitScript {

    // initializes the testcontainer of an empty postgres instance, and kick of an init script
    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            // this will spin up an instance of {@link org.testcontainers.containers.PostgreSQLContainer}
            TestPropertyValues.of(
                    "spring.datasource.url=jdbc:tc:postgresql://hostname/databasename?TC_INITSCRIPT=game-db-prep.sql",
                    "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
                    "spring.jmx.default-domain=app" + new Random().nextInt())
                    .applyTo(applicationContext);
        }
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

