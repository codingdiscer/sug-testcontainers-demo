package sug.testcontainers.demo.service;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.GenericContainer;

import java.time.LocalTime;
import java.util.Random;

/**
 * Helper class that knows how to look for a existing data source,
 * or spin up a docker instance otherwise.
 */
@Slf4j
public class GameServiceTestHelper {

    static final String DB_IMAGE = "sug-testcontainers-demo-db:testdata";
    static final String DB_URL_TEMPLATE = "jdbc:postgresql://%s:%d/postgres?loggerLevel=OFF";
    static final String DB_DRIVER = "org.postgresql.Driver";
    static final String DB_LOCAL_USERNAME = "postgres";
    static final String DB_LOCAL_PASSWORD = "postgres";
    static final String DB_LOCAL_HOSTNAME = "localhost";
    static final int DB_PORT = 5432;
    static final long DB_CHECK_WAIT_TIME_MS = 500;
    static final long DB_MAX_WAIT_TIME_SECONDS = 10;
    static final String VALIDATION_QUERY = "SELECT 1";

    // supporting objects
    static GenericContainer postgres;   // spin up a postgres instance if we can't find one at localhost
    static DriverManagerDataSource dataSource;  // keep a reference to the datasource that was actually used


    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            try {
                // create the connection to the db
                dataSource = connectToDb();

                // prepare the props for spring data
                TestPropertyValues.of(
                        "spring.datasource.url=" + dataSource.getUrl(),
                        "spring.datasource.username=" + dataSource.getUsername(),
                        "spring.datasource.password=" + dataSource.getPassword(),
                        "spring.datasource.driver-class-name=" + DB_DRIVER,
                        "spring.jmx.default-domain=app" + new Random().nextInt())
                        .applyTo(applicationContext);
            } catch(Exception e) {
                log.info("Exception caught during initialization :: " + e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Jumps through the necessary hoops to connect to a db instance.
     * First - looks for an instance running on the default port...
     * Second - spins up a testcontainer docker image and waits for that to be ready...
     *   ...if both of those attempts fail, an exception will be thrown
     */
    static DriverManagerDataSource connectToDb() throws InterruptedException {
        DriverManagerDataSource localDb = new DriverManagerDataSource();

        try {
            // first try on localhost...
            localDb.setDriverClassName(DB_DRIVER);
            localDb.setUrl(String.format(DB_URL_TEMPLATE, DB_LOCAL_HOSTNAME, DB_PORT));
            localDb.setUsername(DB_LOCAL_USERNAME);
            localDb.setPassword(DB_LOCAL_PASSWORD);
            // execute a validation query - this will either succeed quietly or throw an exception
            new JdbcTemplate(localDb).queryForRowSet(VALIDATION_QUERY);
        } catch(RuntimeException e) {
            if(e.getMessage().contains("Connection to localhost:5432 refused.")) {
                // spin up the docker image
                postgres = new GenericContainer(DB_IMAGE).withExposedPorts(DB_PORT);
                postgres.start();
                // update the data source
                localDb.setUrl(String.format(DB_URL_TEMPLATE, postgres.getContainerIpAddress(), postgres.getMappedPort(DB_PORT)));
                // wait for the db to be queryable
                waitForDb(localDb);
            } else {
                log.error("Connection to database failed with exception", e);
                // doh!  this didn't work - toss it up
                throw e;
            }
        }
        return localDb;
    }

    /**
     * Polls the db using on the given data source to verify that the data source is queryable,
     * or throw an exception if the max amount of time is spent waiting.  This is necessary because
     * the docker container declares itself ready (isRunning=true) before the db instance is
     * ready to take queries...thus, we poll until the validation query returns successfully.
     */
    static void waitForDb(DriverManagerDataSource db) throws InterruptedException {
        LocalTime startTime = LocalTime.now();
        while(true) {
            try {
                // execute a validation query - this will either succeed quietly or throw an exception
                new JdbcTemplate(db).queryForRowSet(VALIDATION_QUERY);
                // if we get this far, it means the validation query returned successfully!
                return;
            } catch(Exception e) {
                if(LocalTime.now().isBefore(startTime.plusSeconds(DB_MAX_WAIT_TIME_SECONDS))) {
                    log.info("Still waiting for the db to be ready...");
                    Thread.sleep(DB_CHECK_WAIT_TIME_MS);
                } else {
                    throw e;
                }
            }
        }
    }


    @AfterClass
    public void cleanup() throws Exception {
        log.info("cleanup() :: dataSource" + dataSource + "; postgres=" + postgres);
        if(dataSource != null) {
            dataSource.getConnection().close();
        }
        if(postgres != null) {
            postgres.close();
        }
    }

}
