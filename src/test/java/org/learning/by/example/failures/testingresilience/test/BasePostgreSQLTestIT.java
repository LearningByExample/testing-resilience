package org.learning.by.example.failures.testingresilience.test;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = {BasePostgreSQLTestIT.Initializer.class})
public class BasePostgreSQLTestIT {
    @Autowired
    private ConnectionFactory connectionFactory;

    protected void loadSQL(final String fileName) throws Exception {
        final File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(fileName)).getFile());
        final String content = new String(Files.readAllBytes(file.toPath()));
        final DatabaseClient client = DatabaseClient.create(connectionFactory);
        client.execute(content)
            .fetch()
            .rowsUpdated()
            .as(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete();
    }

    private static final String DATABASE_NAME = "offers";
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "";

    private static final String HOST_CONTEXT_VARIABLE = "offers-datasource.host";
    private static final String PORT_CONTEXT_VARIABLE = "offers-datasource.port";

    @SuppressWarnings("rawtypes")
    @Container
    public static final PostgreSQLStickRandomPortContainer dbContainer = new PostgreSQLStickRandomPortContainer<>()
        .withUsername(DEFAULT_USER)
        .withPassword(DEFAULT_PASSWORD)
        .withDatabaseName(DATABASE_NAME);

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                HOST_CONTEXT_VARIABLE + "=" + dbContainer.getContainerIpAddress(),
                PORT_CONTEXT_VARIABLE + "=" + dbContainer.getRandomStickPort()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    protected static void stopDatabase() {
        if (dbContainer.isRunning()) {
            dbContainer.stop();
        }
    }

    protected void startDatabase() {
        if (!dbContainer.isRunning()) {
            dbContainer.start();
        }
    }
}
