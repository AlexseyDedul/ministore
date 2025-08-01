package by.alexdedul.customerapp.controller;

import by.alexdedul.customerapp.client.FavouriteProductsClient;
import by.alexdedul.customerapp.client.ProductReviewsClient;
import by.alexdedul.customerapp.client.ProductsClient;
import by.alexdedul.customerapp.client.exception.ClientBadRequestException;
import by.alexdedul.customerapp.controller.payload.NewProductReviewPayload;
import by.alexdedul.customerapp.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.reactive.result.view.CsrfRequestDataValueProcessor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@RequestMapping("customer/products/{productId:\\d+}")
@Slf4j
public class ProductController {
    private final ProductsClient productsClient;
    private final FavouriteProductsClient favouriteProductsClient;
    private final ProductReviewsClient productReviewsClient;

    @ModelAttribute(name = "product", binding = false)
    public Mono<Product> loadProduct(@PathVariable("productId") int productId) {
        return productsClient.findProduct(productId)
                .switchIfEmpty(Mono.defer(
                        () -> Mono.error(new NoSuchElementException("customer.products.error.not_found"))
                ));
    }

    @GetMapping
    public Mono<String> getProductPage(@ModelAttribute("product") Mono<Product> productMono, Model model) {
        model.addAttribute("inFavourite", false);
        return productMono.flatMap(
                product -> this.productReviewsClient.findProductReviewsByProductId(product.id())
                        .collectList()
                        .doOnNext(productReviews -> model.addAttribute("reviews", productReviews))
                        .then(this.favouriteProductsClient.findFavouriteProductByProductId(product.id())
                                .doOnNext(favouriteProduct -> model.addAttribute("inFavourite", true)))
                        .thenReturn("customer/products/product")
        );
    }

    @PostMapping("add-to-favourites")
    public Mono<String> addProductToFavourites(@ModelAttribute("product") Mono<Product> productMono) {
        return productMono
                .map(Product::id)
                .flatMap(productId -> this.favouriteProductsClient.addProductToFavourites(productId)
                        .thenReturn("redirect:/customer/products/%d".formatted(productId))
                        .onErrorResume(exception -> {
                            log.error(exception.getMessage(), exception);
                            return Mono.just("redirect:/customer/products/%d".formatted(productId));
                        }));
    }

    @PostMapping("remove-from-favourites")
    public Mono<String> deleteProductFromFavourites(@ModelAttribute("product") Mono<Product> productMono) {
        return productMono
                .map(Product::id)
                .flatMap(productId -> favouriteProductsClient.removeProductFromFavourites(productId)
                        .thenReturn("redirect:/customer/products/%d".formatted(productId))
                        .onErrorResume(ex -> {
                            log.error("Error removing product from favourites {}", ex.getMessage());
                            return Mono.just("redirect:/customer/products/%d".formatted(productId));
                        }));
    }

    @PostMapping("create-review")
    public Mono<String> createReview(@ModelAttribute("product") Mono<Product> productMono,
                                     NewProductReviewPayload payload,
                                     Model model,
                                     ServerHttpResponse response) {
        return productMono.flatMap(product ->
                this.productReviewsClient.createProductReview(product.id(), payload.rating(), payload.review())
                        .thenReturn("redirect:/customer/products/%d".formatted(product.id()))
                        .onErrorResume(ClientBadRequestException.class, exception -> {
                            model.addAttribute("inFavourite", false);
                            model.addAttribute("payload", payload);
                            model.addAttribute("errors", exception.getErrors());
                            response.setStatusCode(HttpStatus.BAD_REQUEST);
                            return this.favouriteProductsClient.findFavouriteProductByProductId(product.id())
                                    .doOnNext(favouriteProduct -> model.addAttribute("inFavourite", true))
                                    .thenReturn("customer/products/product");
                        }));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public String handleNoSuchElementException(NoSuchElementException exception,
                                               Model model,
                                               ServerHttpResponse response) {
        model.addAttribute("error", exception.getMessage());
        response.setStatusCode(HttpStatus.NOT_FOUND);
        return "errors/404";
    }

    @ModelAttribute
    public Mono<CsrfToken> csrfToken(ServerWebExchange exchange) {
        return Objects.requireNonNull(exchange.<Mono<CsrfToken>>getAttribute(CsrfToken.class.getName()))
                .doOnSuccess(token -> exchange.getAttributes()
                    .put(CsrfRequestDataValueProcessor.DEFAULT_CSRF_ATTR_NAME, token));
    }
}
