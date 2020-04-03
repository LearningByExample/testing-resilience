package org.learning.by.example.failures.testingresilience.handler;

import org.learning.by.example.failures.testingresilience.repository.Offer;
import org.learning.by.example.failures.testingresilience.service.OffersService;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class OffersHandler {
    final OffersService offersService;

    public OffersHandler(OffersService offersService) {
        this.offersService = offersService;
    }

    @NonNull
    public Mono<ServerResponse> getOffers(final ServerRequest ignore) {
        return ServerResponse.ok().body(offersService.getOffers(), Offer.class);
    }
}
