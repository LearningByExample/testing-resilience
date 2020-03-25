package org.learning.by.example.failures.testingresilience.service;

import org.junit.jupiter.api.Test;
import org.learning.by.example.failures.testingresilience.repository.Offer;
import org.learning.by.example.failures.testingresilience.repository.OffersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@SpringBootTest
class OffersServiceImplTest {
    @Autowired
    OffersService offersService;

    @MockBean
    OffersRepository offersRepository;

    @Test
    void whenGettingOffersAndRegistryWorksWeShouldReturnThem() {
        final List<Offer> offers = Arrays.asList(
            new Offer(1, "Super Bread", 100.0f),
            new Offer(2, "Chocolate Donuts", 1.0f),
            new Offer(3, "Blueberry Muffins", 1.50f),
            new Offer(4, "Croissants", 3.0f)
        );

        when(offersRepository.findAll()).thenReturn(Flux.fromIterable(offers));

        offersService.getOffers()
            .as(StepVerifier::create)
            .expectSubscription()
            .recordWith(ArrayList::new)
            .thenRequest(Long.MAX_VALUE)
            .expectNextCount(4)
            .expectRecordedMatches(offers::containsAll)
            .expectComplete()
            .verify();

        reset(offersRepository);
    }

    @Test
    void whenGettingOffersAndRegistryFailWeShouldFail() {
        when(offersRepository.findAll()).thenReturn(Flux.error(new Exception("something wrong happen")));

        offersService.getOffers()
            .as(StepVerifier::create)
            .expectError()
            .verify();

        reset(offersRepository);
    }
}
