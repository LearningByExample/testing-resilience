/*
 * Copyright 2020 Learning by Example maintainers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.learning.by.example.failures.testingresilience;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.time.temporal.ChronoUnit.SECONDS;


@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
@ContextConfiguration(initializers = {ActuatorTestIT.Initializer.class})
public class ActuatorTestIT {
    @Autowired
    WebTestClient client;

    private static int getRandomPort(int low, int high) {
        return new Random().nextInt(high - low) + low;
    }

    private static final int POSTGRESQL_DEFAULT_PORT = 5432;
    private static final int LOW_DB_PORT = 30000;
    private static final int HIGH_DB_PORT = 40000;
    private final static int DB_RANDOM_PORT = getRandomPort(LOW_DB_PORT, HIGH_DB_PORT);

    public static final String POSTGRES_DOCKER_IMAGE = "postgres:9.6.12";
    private static final String DATABASE_ENV = "POSTGRES_DB";
    private static final String USER_ENV = "POSTGRES_USER";
    private static final String PASSWORD_ENV = "POSTGRES_PASSWORD";
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DATABASE_NAME = "offers";
    private static final List<String> IMAGE_COMMAND = Arrays.asList("postgres", "-c", "fsync=off");
    private static final String LOG_DB_READY_REGEX = ".*database system is ready to accept connections.*\\s";
    private static final int WAIT_TIMES = 2;
    private static final int WAIT_SECONDS = 60;

    private static final String HOST_CONTEXT_VARIABLE = "offers-datasource.host";
    private static final String PORT_CONTEXT_VARIABLE = "offers-datasource.port";

    public static final String ACTUATOR_STATUS_DOWN = "DOWN";
    public static final String ACTUATOR_STATUS_UP = "UP";
    public static final String ACTUATOR_HEALTH_PATH = "/actuator/health";
    public static final String ACTUATOR_STATUS_JSON_PATH = "$.status";

    @SuppressWarnings("rawtypes")
    @Container
    public static final FixedHostPortGenericContainer postgreSQLContainer = new FixedHostPortGenericContainer<>(POSTGRES_DOCKER_IMAGE)
        .withEnv(DATABASE_ENV, DATABASE_NAME)
        .withEnv(USER_ENV, DEFAULT_USER)
        .withEnv(PASSWORD_ENV, DEFAULT_PASSWORD)
        .withFixedExposedPort(DB_RANDOM_PORT, POSTGRESQL_DEFAULT_PORT)
        .withCommand(IMAGE_COMMAND.toArray(new String[0]))
        .waitingFor(new LogMessageWaitStrategy()
            .withRegEx(LOG_DB_READY_REGEX)
            .withTimes(WAIT_TIMES)
            .withStartupTimeout(Duration.of(WAIT_SECONDS, SECONDS)));


    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                HOST_CONTEXT_VARIABLE + "=" + postgreSQLContainer.getContainerIpAddress(),
                PORT_CONTEXT_VARIABLE + "=" + DB_RANDOM_PORT
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Test
    void whenDatabaseIsUpActuatorShouldBeUP() {
        startDatabase();
        assertThatActuatorIsUp();
    }

    @Test
    void whenDatabaseIsDownActuatorShouldBeDown() {
        stopDatabase();
        assertThatActuatorIsDown();
    }

    @Test
    void whenDatabaseIsDownAndThenUpActuatorShouldBeUp() {
        stopDatabase();
        assertThatActuatorIsDown();

        startDatabase();
        assertThatActuatorIsUp();
    }

    private static void stopDatabase() {
        if (postgreSQLContainer.isRunning()) {
            postgreSQLContainer.stop();
        }
    }

    private void startDatabase() {
        if (!postgreSQLContainer.isRunning()) {
            postgreSQLContainer.start();
        }
    }

    void assertActuator(final HttpStatus status) {
        final String actuatorStatusText;

        if (status != HttpStatus.OK) {
            actuatorStatusText = ACTUATOR_STATUS_DOWN;
        } else {
            actuatorStatusText = ACTUATOR_STATUS_UP;
        }

        client.get()
            .uri(ACTUATOR_HEALTH_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectStatus().isEqualTo(status)
            .expectBody()
            .jsonPath(ACTUATOR_STATUS_JSON_PATH).isEqualTo(actuatorStatusText);
    }

    void assertThatActuatorIsUp() {
        assertActuator(HttpStatus.OK);
    }

    private void assertThatActuatorIsDown() {
        assertActuator(HttpStatus.SERVICE_UNAVAILABLE);
    }
}
