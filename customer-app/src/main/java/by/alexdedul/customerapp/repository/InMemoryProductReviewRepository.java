package by.alexdedul.customerapp.repository;

import by.alexdedul.customerapp.entity.ProductReview;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Repository
public class InMemoryProductReviewRepository implements ProductReviewRepository {
    private final List<ProductReview> productReviews = Collections.synchronizedList(new LinkedList<>());

    @Override
    public Mono<ProductReview> save(ProductReview productReview) {
        productReviews.add(productReview);
        return Mono.just(productReview);
    }

    @Override
    public Flux<ProductReview> findAllByProductId(int productId) {
        return Flux.fromIterable(productReviews)
                .filter(productReview ->
                        productReview.getProductId() == productId);
    }
}
