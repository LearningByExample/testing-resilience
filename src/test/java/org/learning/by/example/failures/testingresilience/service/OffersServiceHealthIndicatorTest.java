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

package org.learning.by.example.failures.testingresilience.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
public class OffersServiceHealthIndicatorTest {
    private static final String ACTUATOR_STATUS_DOWN = "DOWN";
    private static final String ACTUATOR_STATUS_UP = "UP";
    private static final String ACTUATOR_HEALTH_PATH = "/actuator/health";
    private static final String ACTUATOR_STATUS_JSON_PATH = "$.status";

    @Autowired
    WebTestClient client;

    @MockBean
    OffersService offersService;

    @Test
    @DisplayName("When the offers service is not ready actuator is down")
    void whenTheOffersServiceIsNotReadyActuatorIsDown() {
        when(offersService.isReady()).thenReturn(Mono.just(false));

        assertThatActuatorIsDown();

        reset(offersService);
    }

    @Test
    @DisplayName("When the offers service is ready actuator is up")
    void whenTheOffersServiceIsReadyActuatorIsUp() {
        when(offersService.isReady()).thenReturn(Mono.just(true));

        assertThatActuatorIsUp();

        reset(offersService);
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
