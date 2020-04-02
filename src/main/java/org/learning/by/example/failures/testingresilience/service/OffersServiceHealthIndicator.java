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

    private static Health readyToHealth(final boolean ready) {
        final Health.Builder healthBuilder = new Health.Builder();
        final Health.Builder status = ready ? healthBuilder.up() : healthBuilder.down();
        return status.build();
    }

    @Override
    public Mono<Health> health() {
        return offersService.isReady().map(OffersServiceHealthIndicator::readyToHealth);
    }
}
