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

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@SpringBootTest
class OffersServiceImplTest {
    @Autowired
    OffersServiceImpl offersService;

    @MockBean
    OffersRepository offersRepository;

    private static final List<Offer> MOCK_OFFERS = Arrays.asList(
        new Offer(1, "Super Bread", 100.0f),
        new Offer(2, "Chocolate Donuts", 1.0f),
        new Offer(3, "Blueberry Muffins", 1.50f),
        new Offer(4, "Croissants", 3.0f)
    );
    private static final List<Offer> FALLBACK_OFFERS = Arrays.asList(
        new Offer(1, "Fallback 1", 100.0f),
        new Offer(2, "Fallback 2", 1.0f)
    );

    @Test
    @DisplayName("Getting offers repository works and fallback is empty")
    void gettingOffersRepositoryWorksAndFallbackIsEmpty() {
        offersService.reset();

        when(offersRepository.findAll()).thenReturn(Flux.fromIterable(MOCK_OFFERS));

        offersService.getOffers()
            .as(StepVerifier::create)
            .expectSubscription()
            .recordWith(ArrayList::new)
            .thenRequest(Long.MAX_VALUE)
            .expectNextCount(MOCK_OFFERS.size())
            .expectRecordedMatches(MOCK_OFFERS::containsAll)
            .expectComplete()
            .verify();

        reset(offersRepository);
    }

    @Test
    @DisplayName("Getting offers repository works and fallback is not empty")
    void gettingOffersRepositoryWorksAndFallbackIsNotEmpty() {
        offersService.reset();

        when(offersRepository.findAll()).thenReturn(Flux.fromIterable(FALLBACK_OFFERS));

        offersService.isReady()
            .as(StepVerifier::create)
            .expectSubscription()
            .expectNext(true)
            .expectComplete()
            .verify();

        reset(offersRepository);

        when(offersRepository.findAll()).thenReturn(Flux.fromIterable(MOCK_OFFERS));

        offersService.getOffers()
            .as(StepVerifier::create)
            .expectSubscription()
            .recordWith(ArrayList::new)
            .thenRequest(Long.MAX_VALUE)
            .expectNextCount(MOCK_OFFERS.size())
            .expectRecordedMatches(MOCK_OFFERS::containsAll)
            .expectComplete()
            .verify();

        reset(offersRepository);
    }

    @Test
    @DisplayName("Getting offers repository fails and Fallback is empty")
    void gettingOffersRepositoryFailsAndFallbackIsEmpty() {
        offersService.reset();
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
        offersService.reset();

        when(offersRepository.findAll()).thenReturn(Flux.fromIterable(FALLBACK_OFFERS));

        offersService.isReady()
            .as(StepVerifier::create)
            .expectSubscription()
            .expectNext(true)
            .expectComplete()
            .verify();

        reset(offersRepository);

        when(offersRepository.findAll()).thenReturn(Flux.error(new Exception("something wrong happen")));

        offersService.getOffers()
            .as(StepVerifier::create)
            .expectSubscription()
            .recordWith(ArrayList::new)
            .thenRequest(Long.MAX_VALUE)
            .expectNextCount(FALLBACK_OFFERS.size())
            .expectRecordedMatches(FALLBACK_OFFERS::containsAll)
            .expectComplete()
            .verify();

        reset(offersRepository);
    }


    @Test
    @DisplayName("is ready should work based if we have fallback once")
    void isReadyShouldWorkBasedIfWeHaveFallback() {
        offersService.reset();

        when(offersRepository.findAll()).thenReturn(Flux.error(new Exception("something wrong happen")));

        offersService.isReady()
            .as(StepVerifier::create)
            .expectSubscription()
            .expectNext(false)
            .expectComplete()
            .verify();

        reset(offersRepository);

        when(offersRepository.findAll()).thenReturn(Flux.fromIterable(FALLBACK_OFFERS));

        offersService.isReady()
            .as(StepVerifier::create)
            .expectSubscription()
            .expectNext(true)
            .expectComplete()
            .verify();

        reset(offersRepository);

        when(offersRepository.findAll()).thenReturn(Flux.error(new Exception("something wrong happen")));

        offersService.isReady()
            .as(StepVerifier::create)
            .expectSubscription()
            .expectNext(true)
            .expectComplete()
            .verify();

        reset(offersRepository);
    }
}
