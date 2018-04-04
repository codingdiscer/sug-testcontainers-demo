package sug.testcontainers.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import sug.testcontainers.demo.DemoApplication;
import sug.testcontainers.demo.model.Game;
import sug.testcontainers.demo.model.GameComplexity;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Performs an integration test starting with a custom docker image that contains just a
 * schema and empty tables
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = GameService3DbBase.Initializer.class, classes = { DemoApplication.class })
@Slf4j
public class GameService3DbBase {

    @ClassRule
    static public GenericContainer postgres =
            new GenericContainer("sug-testcontainers-demo-db:base");


    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            try {
                // point the data source at the docker container
                DriverManagerDataSource dataSource = new DriverManagerDataSource();
                dataSource.setDriverClassName(GameServiceTestHelper.DB_DRIVER);     //"org.postgresql.Driver"
                dataSource.setUrl(String.format(GameServiceTestHelper.DB_URL_TEMPLATE,
                        GameServiceTestHelper.DB_LOCAL_HOSTNAME,
                        postgres.getMappedPort(GameServiceTestHelper.DB_PORT)));    //jdbc:postgresql://localhost:{port}/postgres
                dataSource.setUsername(GameServiceTestHelper.DB_LOCAL_USERNAME);    //"postgres"
                dataSource.setPassword(GameServiceTestHelper.DB_LOCAL_PASSWORD);    //"postgres"

                // wait for the db to be fully ready
                GameServiceTestHelper.waitForDb(dataSource);

                // prepare the props for spring data
                TestPropertyValues.of(
                        "spring.datasource.url=" + dataSource.getUrl(),
                        "spring.datasource.username=" + dataSource.getUsername(),
                        "spring.datasource.password=" + dataSource.getPassword(),
                        "spring.datasource.driver-class-name=" + GameServiceTestHelper.DB_DRIVER,
                        "spring.jmx.default-domain=app" + new Random().nextInt())
                        .applyTo(applicationContext);
            } catch(Exception e) {
                log.info("Exception caught during initialization :: " + e);
                throw new RuntimeException(e);
            }
        }
    }


    // the class under test
    @Autowired GameService gameService;

    // interesting to poke a stick at
    @Autowired DataSource dataSource;


    boolean initComplete = false;


    @Before
    public void loadTestData() throws Exception {
        if(!initComplete) {
            Connection connection = dataSource.getConnection();
            connection.prepareStatement("insert into game (game_name, complexity) values ('Sorry', 0)").execute();
            connection.prepareStatement("insert into game (game_name, complexity) values ('King Of New York', 1)").execute();
            connection.prepareStatement("insert into game (game_name, complexity) values ('Mage Knight', 2)").execute();
            initComplete = true;
        }
    }


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
