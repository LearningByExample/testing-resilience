package org.learning.by.example.failures.testingresilience.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.learning.by.example.failures.testingresilience.test.BasePostgreSQLTestIT;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class OffersRepositoryTestIT extends BasePostgreSQLTestIT {
    @Autowired
    OffersRepository repository;

    @BeforeEach
    void initData() throws Exception {
        startDatabase();
        loadSQL("sql/schema.sql");
        loadSQL("sql/data.sql");
    }

    @Test
    @DisplayName("When requesting all offers we should get them all")
    void whenRequestingAllOffersWeShouldGetThemAll() {
        final List<Offer> offers = Arrays.asList(
            new Offer(1, "Super Bread", 100.0f),
            new Offer(2, "Chocolate Donuts", 1.0f),
            new Offer(3, "Blueberry Muffins", 1.50f),
            new Offer(4, "Croissants", 3.0f)
        );

        repository.findAll()
            .as(StepVerifier::create)
            .expectSubscription()
            .recordWith(ArrayList::new)
            .thenRequest(Long.MAX_VALUE)
            .expectNextCount(4)
            .expectRecordedMatches(offers::containsAll)
            .expectComplete()
            .verify();
    }

    @Test
    @DisplayName("When requesting all and database stop weShould get an error")
    void whenRequestingAllAndDatabaseStopWeShouldGetAnError() {
        stopDatabase();
        repository.findAll()
            .as(StepVerifier::create)
            .expectError()
            .verify();
    }
}
