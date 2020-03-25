package org.learning.by.example.failures.testingresilience.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OffersRepository extends R2dbcRepository<Offer, Integer> {
    Flux<Offer> findAll();
}
