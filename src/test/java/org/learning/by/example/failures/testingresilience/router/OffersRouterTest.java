package org.learning.by.example.failures.testingresilience.router;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.learning.by.example.failures.testingresilience.repository.Offer;
import org.learning.by.example.failures.testingresilience.service.OffersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
class OffersRouterTest {
    @Autowired
    WebTestClient webTestClient;

    @MockBean
    OffersService offersService;

    private static final List<Offer> MOCK_OFFERS = Arrays.asList(
        new Offer(1, "Super Bread", 100.0f),
        new Offer(2, "Chocolate Donuts", 1.0f),
        new Offer(3, "Blueberry Muffins", 1.50f),
        new Offer(4, "Croissants", 3.0f)
    );

    @Test
    @DisplayName("We should return all offers")
    void weShouldReturnAllOffers() {
        when(offersService.getOffers()).thenReturn(Flux.fromIterable(MOCK_OFFERS));

        webTestClient.get()
            .uri("/offers")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectStatus().isOk()
            .expectBodyList(Offer.class)
            .value(offers -> assertThat(offers).hasSize(MOCK_OFFERS.size()).containsAll(MOCK_OFFERS));

        reset(offersService);
    }
}
