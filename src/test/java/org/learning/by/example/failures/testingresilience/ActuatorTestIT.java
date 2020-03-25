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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
@ContextConfiguration(initializers = {ActuatorTestIT.Initializer.class})
public class ActuatorTestIT {
    @Autowired
    WebTestClient client;

    private static final String DATABASE_NAME = "offers";
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "";


    private static final String HOST_CONTEXT_VARIABLE = "offers-datasource.host";
    private static final String PORT_CONTEXT_VARIABLE = "offers-datasource.port";

    public static final String ACTUATOR_STATUS_DOWN = "DOWN";
    public static final String ACTUATOR_STATUS_UP = "UP";
    public static final String ACTUATOR_HEALTH_PATH = "/actuator/health";
    public static final String ACTUATOR_STATUS_JSON_PATH = "$.status";

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
        if (dbContainer.isRunning()) {
            dbContainer.stop();
        }
    }

    private void startDatabase() {
        if (!dbContainer.isRunning()) {
            dbContainer.start();
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
