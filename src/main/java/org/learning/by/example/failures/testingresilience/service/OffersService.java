package org.learning.by.example.failures.testingresilience.service;

import org.learning.by.example.failures.testingresilience.repository.Offer;
import reactor.core.publisher.Flux;

public interface OffersService {
    Flux<Offer> getOffers();

    boolean isReady();
}
