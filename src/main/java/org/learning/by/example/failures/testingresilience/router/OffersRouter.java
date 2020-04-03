package org.learning.by.example.failures.testingresilience.router;

import org.learning.by.example.failures.testingresilience.handler.OffersHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Component
public class OffersRouter {
    final OffersHandler offersHandler;

    public OffersRouter(OffersHandler offersHandler) {
        this.offersHandler = offersHandler;
    }

    @Bean
    RouterFunction<ServerResponse> offers() {
        return route(GET("/offers"), offersHandler::getOffers);
    }
}
