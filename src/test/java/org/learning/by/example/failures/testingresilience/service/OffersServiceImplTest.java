package org.learning.by.example.failures.testingresilience.service;

import org.junit.jupiter.api.DisplayName;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@SpringBootTest
class OffersServiceImplTest {
    @Autowired
    OffersServiceImpl offersService;

    @MockBean
    OffersRepository offersRepository;

    @Test
    @DisplayName("Getting offers repository works and fall back is empty")
    void gettingOffersRepositoryWorksAndFallBackIsEmpty() {
        offersService.clearFallBack();
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
            .expectNextCount(offers.size())
            .expectRecordedMatches(offers::containsAll)
            .expectComplete()
            .verify();

        reset(offersRepository);
    }

    @Test
    @DisplayName("Getting offers repository works and fall back is not empty")
    void gettingOffersRepositoryWorksAndFallBackIsNotEmpty() {
        offersService.clearFallBack();

        final List<Offer> fallBackOffers = Arrays.asList(
            new Offer(1, "FallBack1", 100.0f),
            new Offer(2, "Fall Back 2", 1.0f)
        );

        when(offersRepository.findAll()).thenReturn(Flux.fromIterable(fallBackOffers));

        offersService.prepareFallBack();

        reset(offersRepository);

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
            .expectNextCount(offers.size())
            .expectRecordedMatches(offers::containsAll)
            .expectComplete()
            .verify();

        reset(offersRepository);
    }

    @Test
    @DisplayName("Getting offers repository fails and Fallback is empty")
    void gettingOffersRepositoryFailsAndFallBackIsEmpty() {
        offersService.clearFallBack();
        when(offersRepository.findAll()).thenReturn(Flux.error(new Exception("something wrong happen")));

        offersService.getOffers()
            .as(StepVerifier::create)
            .recordWith(ArrayList::new)
            .thenRequest(Long.MAX_VALUE)
            .expectNextCount(0)
            .expectComplete()
            .verify();

        reset(offersRepository);
    }

    @Test
    @DisplayName("Getting offers repository fails and Fallback is not empty")
    void gettingOffersRepositoryFailsAndFallBackIsNotEmpty() {
        offersService.clearFallBack();

        final List<Offer> fallBackOffers = Arrays.asList(
            new Offer(1, "FallBack1", 100.0f),
            new Offer(2, "Fall Back 2", 1.0f)
        );

        when(offersRepository.findAll()).thenReturn(Flux.fromIterable(fallBackOffers));

        offersService.prepareFallBack();

        reset(offersRepository);

        when(offersRepository.findAll()).thenReturn(Flux.error(new Exception("something wrong happen")));

        offersService.getOffers()
            .as(StepVerifier::create)
            .expectSubscription()
            .recordWith(ArrayList::new)
            .thenRequest(Long.MAX_VALUE)
            .expectNextCount(fallBackOffers.size())
            .expectRecordedMatches(fallBackOffers::containsAll)
            .expectComplete()
            .verify();

        reset(offersRepository);
    }


    @Test
    @DisplayName("is ready should work based if we have fallback")
    void isReadyShouldWorkBasedIfWeHaveFallback() {
        offersService.clearFallBack();

        when(offersRepository.findAll()).thenReturn(Flux.error(new Exception("something wrong happen")));

        assertThat(offersService.isReady()).isFalse();

        reset(offersRepository);

        offersService.clearFallBack();
        final List<Offer> fallBackOffers = Arrays.asList(
            new Offer(1, "FallBack1", 100.0f),
            new Offer(2, "Fall Back 2", 1.0f)
        );

        when(offersRepository.findAll()).thenReturn(Flux.fromIterable(fallBackOffers));

        assertThat(offersService.isReady()).isTrue();

        reset(offersRepository);
    }
}
