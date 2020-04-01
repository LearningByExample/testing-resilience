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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.learning.by.example.failures.testingresilience.test.BasePostgreSQLTestIT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureWebTestClient
public class ActuatorTestIT extends BasePostgreSQLTestIT {
    private static final String ACTUATOR_STATUS_DOWN = "DOWN";
    private static final String ACTUATOR_STATUS_UP = "UP";
    private static final String ACTUATOR_HEALTH_PATH = "/actuator/health";
    private static final String ACTUATOR_STATUS_JSON_PATH = "$.status";

    @Autowired
    WebTestClient client;

    @Test
    @DisplayName("Once we have connect to the database we should not fail when going down")
    void whenDatabaseIsUpActuatorShouldBeUP() throws Exception {
        stopDatabase();
        assertThatActuatorIsDown();

        startDatabase();
        loadSQL("sql/schema.sql");
        loadSQL("sql/data.sql");
        assertThatActuatorIsUp();

        stopDatabase();
        assertThatActuatorIsUp();
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
