package org.learning.by.example.failures.testingresilience.service;

import org.learning.by.example.failures.testingresilience.repository.Offer;
import org.learning.by.example.failures.testingresilience.repository.OffersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class OffersServiceImpl implements OffersService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OffersServiceImpl.class);
    private final ConcurrentLinkedDeque<Offer> offersFallBack;
    private final OffersRepository offersRepository;

    public OffersServiceImpl(final OffersRepository offersRepository) {
        this.offersRepository = offersRepository;
        offersFallBack = new ConcurrentLinkedDeque<>();
    }

    public void prepareFallBack() {
        final List<Offer> offers = offersRepository.findAll()
            .onErrorResume(throwable -> {
                LOGGER.warn("error getting fallback offers", throwable);
                return Flux.empty();
            })
            .collectList().block();

        if ((offers != null) && (offers.size() > 0)) {
            clearFallBack();
            offersFallBack.addAll(offers);
        }
    }

    public void clearFallBack() {
        offersFallBack.clear();
    }

    @Override
    public Flux<Offer> getOffers() {
        return offersRepository.findAll().onErrorResume(ignore -> Flux.fromIterable(offersFallBack));
    }

    @Override
    public boolean isReady() {
        if (offersFallBack.size() == 0) {
            prepareFallBack();
        }
        return offersFallBack.size() > 0;
    }
}
