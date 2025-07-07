package by.alexdedul.feedbackservice.controller;

import by.alexdedul.feedbackservice.controller.payload.NewFavouriteProductPayload;
import by.alexdedul.feedbackservice.entity.FavouriteProduct;
import by.alexdedul.feedbackservice.service.FavouriteProductsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("feedback-api/favourite-products")
public class FavouriteProductsRestController {
    private final FavouriteProductsService favouriteProductsService;

    @GetMapping
    public Flux<FavouriteProduct> findFavouriteProducts() {
        return favouriteProductsService.findFavouriteProducts();
    }

    @GetMapping("by-product-id/{productId:\\d+}")
    public Mono<FavouriteProduct> findFavouriteProductById(@PathVariable("productId") Integer productId) {
        return favouriteProductsService.findFavouriteProductByProduct(productId);
    }

    @PostMapping
    public Mono<ResponseEntity<FavouriteProduct>> createFavouriteProduct(
            @Valid @RequestBody Mono<NewFavouriteProductPayload> payloadMono,
            UriComponentsBuilder uriComponentsBuilder) {
        return payloadMono
                .flatMap(payload -> favouriteProductsService.addProductToFavourites(payload.productId()))
                .map(favouriteProduct -> ResponseEntity
                        .created(uriComponentsBuilder.replacePath("feedback-api/favourite-products/{id}")
                                .build(favouriteProduct.getId()))
                        .body(favouriteProduct));
    }

    @DeleteMapping("by-product-id/{productId:\\d+}")
    public Mono<ResponseEntity<Void>> removeProductFromFavourites(@PathVariable("productId") Integer productId) {
        return favouriteProductsService.removeProductFromFavourites(productId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}
