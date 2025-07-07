package by.alexdedul.customerapp.controller;

import by.alexdedul.customerapp.client.ProductsClient;
import by.alexdedul.customerapp.controller.payload.NewProductReviewPayload;
import by.alexdedul.customerapp.entity.Product;
import by.alexdedul.customerapp.service.FavouriteProductsService;
import by.alexdedul.customerapp.service.ProductReviewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
@RequestMapping("customer/products/{productId:\\d+}")
public class ProductController {
    private final ProductsClient productsClient;
    private final FavouriteProductsService favouriteProductsService;
    private final ProductReviewsService productReviewsService;

    @ModelAttribute(name = "product", binding = false)
    public Mono<Product> loadProduct(@PathVariable("productId") int productId) {
        return productsClient.findProduct(productId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("customer.products.error.not_found")));
    }

    @GetMapping
    public Mono<String> getProductPage(@PathVariable("productId") int id, Model model) {
        model.addAttribute("inFavourite", false);
        return productReviewsService.findProductReviewsByProduct(id)
                .collectList()
                .doOnNext(productReviews ->
                        model.addAttribute("reviews", productReviews))
                .then(favouriteProductsService.findFavouriteProductByProduct(id)
                        .doOnNext(favouriteProduct ->
                                model.addAttribute("inFavourite", true)))
                .thenReturn("customer/products/product");
    }

    @PostMapping("add-to-favourites")
    public Mono<String> addProductToFavourites(@ModelAttribute("product") Mono<Product> productMono) {
        return productMono
                .map(Product::id)
                .flatMap(productId -> favouriteProductsService.addProductToFavourites(productId)
                        .thenReturn("redirect:/customer/products/%d".formatted(productId)));
    }

    @PostMapping("remove-from-favourites")
    public Mono<String> deleteProductFromFavourites(@ModelAttribute("product") Mono<Product> productMono) {
        return productMono
                .map(Product::id)
                .flatMap(productId -> favouriteProductsService.removeProductFromFavourites(productId)
                        .thenReturn("redirect:/customer/products/%d".formatted(productId)));
    }

    @PostMapping("create-review")
    public Mono<String> createReview(@PathVariable("productId") int id,
                                     @Valid NewProductReviewPayload payload,
                                     BindingResult result,
                                     Model model) {
        if (result.hasErrors()) {
            model.addAttribute("inFavourite", false);
            model.addAttribute("payload", payload);
            model.addAttribute("errors", result.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .toList());
            return favouriteProductsService.findFavouriteProductByProduct(id)
                    .doOnNext(favouriteProduct ->
                            model.addAttribute("inFavourite", true))
                    .thenReturn("customer/products/product");
        }else {
            return productReviewsService.createProductReview(id, payload.rating(), payload.review())
                    .thenReturn("redirect:/customer/products/%d".formatted(id));
        }
    }

    @ExceptionHandler(NoSuchElementException.class)
    public String handleNoSuchElementException(NoSuchElementException exception, Model model) {
        model.addAttribute("error", exception.getMessage());
        return "errors/404";
    }
}
