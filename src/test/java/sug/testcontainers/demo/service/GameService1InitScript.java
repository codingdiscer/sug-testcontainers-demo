package sug.testcontainers.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import sug.testcontainers.demo.DemoApplication;
import sug.testcontainers.demo.config.DemoConfig;
import sug.testcontainers.demo.dao.GameRepository;



@ContextConfiguration(initializers = GameService1InitScript.Initializer.class, classes = {
        // the whole app
        DemoApplication.class

        // just the specific components to test
//        DemoConfig.class, GameRepository.class, GameService.class
} )
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Slf4j
public class GameService1InitScript {


    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of("spring.datasource.url=" + postgres.getJdbcUrl(), //
                    "spring.datasource.username=" + postgres.getUsername(), //
                    "spring.datasource.password=" + postgres.getPassword()) //
                    .applyTo(applicationContext);
        }
    }

    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("sug-testcontainers-demo-db:base");

    @RegisterExtension
    static SpringTestContainersExtension extension = new SpringTestContainersExtension(postgres, true);





    // the class i want to make calls against
    @Autowired GameService gameService;


    @Test
    public void doSomething()
        throws Exception
    {

        log.info("doSomething() :: gameService=" + gameService);
        log.info("doSomething() :: postgres=" + postgres);
        log.info("doSomething() :: postgres.isRunning=" + postgres.isRunning());
        log.info("doSomething() :: postgres.jdbcUrl=" + postgres.getJdbcUrl());
        log.info("doSomething() :: postgres.username=" + postgres.getUsername());
        log.info("doSomething() :: postgres.password=" + postgres.getPassword());
        log.info("doSomething() :: postgres.exposedPort=" + postgres.getExposedPorts().get(0));
        log.info("doSomething() :: postgres.mappedPort =" + postgres.getMappedPort(5432));

        // sleep to allow time to check the db
        Thread.sleep(240000);


        assert("a".equals("a"));

        // do some useful test here against the GameService


    }

}

