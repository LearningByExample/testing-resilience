package org.learning.by.example.failures.testingresilience.service;

import org.learning.by.example.failures.testingresilience.repository.Offer;
import org.learning.by.example.failures.testingresilience.repository.OffersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class OffersServiceImpl implements OffersService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OffersServiceImpl.class);
    private static final String CIRCUIT_BREAKER_ID = "OffersServiceImplCircuit";
    private static final List<Offer> emptyOffers = new ArrayList<>();

    private final ConcurrentLinkedDeque<Offer> offersFallback;
    private final OffersRepository offersRepository;
    private final ReactiveCircuitBreaker circuitBreaker;

    @SuppressWarnings("rawtypes")
    public OffersServiceImpl(final OffersRepository offersRepository,
                             final ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory) {
        this.offersRepository = offersRepository;
        this.circuitBreaker = reactiveCircuitBreakerFactory.create(CIRCUIT_BREAKER_ID);
        this.offersFallback = new ConcurrentLinkedDeque<>(emptyOffers);
    }

    private static Flux<Offer> throwableToFallBack(final Throwable throwable, final Iterable<Offer> fallback) {
        LOGGER.warn("error getting offers, returning fallback", throwable);
        return Flux.fromIterable(fallback);
    }

    private Flux<Offer> getOfferWithFallback(final Iterable<Offer> fallback) {
        return circuitBreaker.run(offersRepository.findAll(),
            throwable -> OffersServiceImpl.throwableToFallBack(throwable, fallback));
    }

    private void setFallback(final Collection<Offer> fallBack) {
        reset();
        this.offersFallback.addAll(fallBack);
    }

    private boolean offersListToBoolean(final List<Offer> offers) {
        if (offers.size() > 0) {
            setFallback(offers);
        }
        return this.offersFallback.size() > 0;
    }

    @Override
    public Mono<Boolean> isReady() {
        return getOfferWithFallback(emptyOffers).collectList().map(this::offersListToBoolean);
    }

    public void reset() {
        this.offersFallback.clear();
    }

    @Override
    public Flux<Offer> getOffers() {
        return getOfferWithFallback(offersFallback);
    }
}
