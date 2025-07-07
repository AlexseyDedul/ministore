package by.alexdedul.feedbackservice.service;

import by.alexdedul.feedbackservice.entity.FavouriteProduct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FavouriteProductsService {

    Mono<FavouriteProduct> addProductToFavourites(int productId);
    Mono<Void> removeProductFromFavourites(int productId);
    Mono<FavouriteProduct> findFavouriteProductByProduct(int productId);

    Flux<FavouriteProduct> findFavouriteProducts();
}
