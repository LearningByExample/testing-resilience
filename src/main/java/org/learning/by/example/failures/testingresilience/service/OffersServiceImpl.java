package org.learning.by.example.failures.testingresilience.service;

import org.learning.by.example.failures.testingresilience.repository.Offer;
import org.learning.by.example.failures.testingresilience.repository.OffersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
    public OffersServiceImpl(final OffersRepository offersRepository, final ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory) {
        this.offersRepository = offersRepository;
        this.circuitBreaker = reactiveCircuitBreakerFactory.create(CIRCUIT_BREAKER_ID);
        this.offersFallback = new ConcurrentLinkedDeque<>(emptyOffers);
    }

    private Flux<Offer> getOfferWithFallback(final Iterable<Offer> fallback) {
        return circuitBreaker.run(offersRepository.findAll(), throwable -> {
            LOGGER.warn("error getting offers, returning fallback", throwable);
            return Flux.fromIterable(fallback);
        });
    }

    public void clearFallback() {
        this.offersFallback.clear();
    }

    public void setFallback(final Collection<Offer> fallBack){
        clearFallback();
        this.offersFallback.addAll(fallBack);
    }

    public void prepareFallback() {
        final List<Offer> offers = getOfferWithFallback(emptyOffers).collectList().block();
        if (Objects.requireNonNull(offers).size() > 0) {
            setFallback(offers);
        }
    }

    private boolean hasFallback() {
        return this.offersFallback.size() > 0;
    }

    @Override
    public boolean isReady() {
        if (!hasFallback()) {
            prepareFallback();
        }
        return hasFallback();
    }

    @Override
    public Flux<Offer> getOffers() {
        return getOfferWithFallback(offersFallback);
    }
}
