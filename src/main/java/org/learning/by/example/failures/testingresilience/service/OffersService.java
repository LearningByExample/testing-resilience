package org.learning.by.example.failures.testingresilience.service;

import org.learning.by.example.failures.testingresilience.repository.Offer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OffersService {
    Flux<Offer> getOffers();

    Mono<Boolean> isReady();
}
