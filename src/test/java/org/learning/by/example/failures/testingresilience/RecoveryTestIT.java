package org.learning.by.example.failures.testingresilience;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.learning.by.example.failures.testingresilience.repository.Offer;
import org.learning.by.example.failures.testingresilience.test.BasePostgreSQLTestIT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
public class RecoveryTestIT extends BasePostgreSQLTestIT {
    private static final String READINESS_PATH = "/actuator/health";

    private static final List<Offer> INITIAL_OFFERS = Arrays.asList(
        new Offer(1, "Super Bread", 100.0f),
        new Offer(2, "Chocolate Donuts", 1.0f),
        new Offer(3, "Blueberry Muffins", 1.50f),
        new Offer(4, "Croissants", 3.0f)
    );

    private static final List<Offer> ADDITIONAL_OFFERS = Arrays.asList(
        new Offer(1, "Super Bread", 100.0f),
        new Offer(2, "Chocolate Donuts", 1.0f),
        new Offer(3, "Blueberry Muffins", 1.50f),
        new Offer(4, "Croissants", 3.0f),
        new Offer(5, "Vanilla Cookies", 0.50f)
    );

    @Autowired
    WebTestClient client;

    @Test
    @DisplayName("Whe should recover")
    void whenShouldRecover() throws Exception {
        stopDatabase();
        assertServiceIsReady(false);

        startDatabase();
        loadInitData();
        assertServiceIsReady(true);
        assertOffersAre(INITIAL_OFFERS);

        stopDatabase();
        assertServiceIsReady(true);
        assertOffersAre(INITIAL_OFFERS);

        startDatabase();
        loadInitData();
        addVanillaCookies();
        assertServiceIsReady(true);
        assertOffersAre(ADDITIONAL_OFFERS);

        stopDatabase();
        assertServiceIsReady(true);
        assertOffersAre(ADDITIONAL_OFFERS);
    }

    void loadInitData() throws Exception {
        loadSQL("sql/schema.sql");
        loadSQL("sql/data.sql");
    }

    void addVanillaCookies() throws Exception {
        loadSQL("sql/more.sql");
    }

    void assertServiceIsReady(boolean ready) {
        final HttpStatus status;
        if (ready) {
            status = HttpStatus.OK;
        } else {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }

        client.get()
            .uri(READINESS_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectStatus().isEqualTo(status);
    }

    void assertOffersAre(final List<Offer> offers) {
        client.get()
            .uri("/offers")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectStatus().isOk()
            .expectBodyList(Offer.class)
            .value(returnOffers -> assertThat(returnOffers).hasSize(offers.size()).containsAll(offers));
    }
}
