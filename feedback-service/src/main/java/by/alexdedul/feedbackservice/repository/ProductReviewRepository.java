package by.alexdedul.feedbackservice.repository;

import by.alexdedul.feedbackservice.entity.ProductReview;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductReviewRepository {
    Mono<ProductReview> save(ProductReview productReview);
    Flux<ProductReview> findAllByProductId(int productId);
}
