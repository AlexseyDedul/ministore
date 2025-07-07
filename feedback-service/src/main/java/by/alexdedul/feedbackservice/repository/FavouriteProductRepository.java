package by.alexdedul.feedbackservice.repository;

import by.alexdedul.feedbackservice.entity.FavouriteProduct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FavouriteProductRepository {
    Mono<FavouriteProduct> save(FavouriteProduct favouriteProduct);

    Mono<Void> deleteByProductId(int productId);

    Mono<FavouriteProduct> findByProductId(int productId);

    Flux<FavouriteProduct> findAll();
}
