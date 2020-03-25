package org.learning.by.example.failures.testingresilience.service;

import org.learning.by.example.failures.testingresilience.repository.Offer;
import org.learning.by.example.failures.testingresilience.repository.OffersRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class OffersServiceImpl implements OffersService {
    private final OffersRepository offersRepository;

    public OffersServiceImpl(final OffersRepository offersRepository) {
        this.offersRepository = offersRepository;
    }

    @Override
    public Flux<Offer> getOffers() {
        return offersRepository.findAll();
    }
}
