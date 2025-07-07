package by.alexdedul.customerapp.client;

import by.alexdedul.customerapp.entity.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductsClient {
    Flux<Product> findAllProducts(String filter);

    Mono<Product> findProduct(int productId);
}
