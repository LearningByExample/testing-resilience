package org.learning.by.example.failures.testingresilience.service;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class OffersServiceHealthIndicator implements ReactiveHealthIndicator {
    final OffersService offersService;

    public OffersServiceHealthIndicator(final OffersService offersService) {
        this.offersService = offersService;
    }

    @Override
    public Mono<Health> health() {
        return offersService.isReady().map(ready -> {
            if (ready) {
                return new Health.Builder().up().build();
            }
            return new Health.Builder().down().build();
        });
    }
}
