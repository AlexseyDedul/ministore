package by.alexdedul.customerapp.controller;

import by.alexdedul.customerapp.client.FavouriteProductsClient;
import by.alexdedul.customerapp.client.ProductReviewsClient;
import by.alexdedul.customerapp.client.ProductsClient;
import by.alexdedul.customerapp.client.exception.ClientBadRequestException;
import by.alexdedul.customerapp.controller.payload.NewProductReviewPayload;
import by.alexdedul.customerapp.entity.FavouriteProduct;
import by.alexdedul.customerapp.entity.Product;
import by.alexdedul.customerapp.entity.ProductReview;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.ui.ConcurrentModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {
    @Mock
    ProductsClient productsClient;
    @Mock
    FavouriteProductsClient favouriteProductsClient;
    @Mock
    ProductReviewsClient productReviewsClient;

    @InjectMocks
    ProductController productController;

    @Test
    void loadProduct_ProductExists_ReturnNotEmptyMono(){
        // given
        var product = new Product(1, "Product 1", "Description 1");
        doReturn(Mono.just(product)).when(productsClient).findProduct(1);

        // when
        StepVerifier.create(productController.loadProduct(1))
                // then
                .expectNext(new Product(1, "Product 1", "Description 1"))
                .expectComplete()
                .verify();

        verify(productsClient).findProduct(1);
        verifyNoMoreInteractions(productsClient);
        verifyNoInteractions(favouriteProductsClient,  productReviewsClient);
    }

    @Test
    void loadProduct_ProductDoesNotExists_ReturnMonoWithNoSuchElementException(){
        // given
        doReturn(Mono.empty()).when(productsClient).findProduct(1);

        // when
        StepVerifier.create(productController.loadProduct(1))
                // then
                .expectErrorMatches(ex -> ex instanceof NoSuchElementException && ex.getMessage().equals("customer.products.error.not_found"))
                .verify();

        verify(productsClient).findProduct(1);
        verifyNoMoreInteractions(productsClient);
        verifyNoInteractions(favouriteProductsClient,  productReviewsClient);
    }
    @Test
    void getProductPage_ReturnsProductPage() {
        // given
        var model = new ConcurrentModel();
        var productReviews = List.of(
                new ProductReview(UUID.fromString("6a8512d8-cbaa-11ee-b986-376cc5867cf5"), 1, 5, "Good"),
                new ProductReview(UUID.fromString("849c3fac-cbaa-11ee-af68-737c6d37214a"), 1, 4, "Not bad"));

        doReturn(Flux.fromIterable(productReviews)).when(this.productReviewsClient).findProductReviewsByProductId(1);

        var favouriteProduct = new FavouriteProduct(UUID.fromString("af5f9496-cbaa-11ee-a407-27b46917819e"), 1);
        doReturn(Mono.just(favouriteProduct)).when(this.favouriteProductsClient).findFavouriteProductByProductId(1);

        // when
        StepVerifier.create(this.productController.getProductPage(
                        Mono.just(new Product(1, "Product 1", "Description 1")), model))
                // then
                .expectNext("customer/products/product")
                .verifyComplete();

        assertEquals(productReviews, model.getAttribute("reviews"));
        assertEquals(true, model.getAttribute("inFavourite"));

        verify(this.productReviewsClient).findProductReviewsByProductId(1);
        verify(this.favouriteProductsClient).findFavouriteProductByProductId(1);
        verifyNoMoreInteractions(this.productsClient, this.favouriteProductsClient);
        verifyNoInteractions(this.productsClient);
    }

    @Test
    void addProductToFavourites_RequestIsValid_RedirectsToProductPage() {
        // given
        doReturn(Mono.just(new FavouriteProduct(UUID.fromString("25ec67b4-cbac-11ee-adc8-4bd80e8171c4"), 1)))
                .when(this.favouriteProductsClient).addProductToFavourites(1);

        // when
        StepVerifier.create(this.productController.addProductToFavourites(
                        Mono.just(new Product(1, "Product 1", "Description 1"))))
                // then
                .expectNext("redirect:/customer/products/1")
                .verifyComplete();

        verify(this.favouriteProductsClient).addProductToFavourites(1);
        verifyNoMoreInteractions(this.favouriteProductsClient);
        verifyNoInteractions(this.productReviewsClient, this.productsClient);
    }

    @Test
    void addProductToFavourites_RequestIsInvalid_RedirectsToProductPage() {
        // given
        doReturn(Mono.error(new ClientBadRequestException(null,
                List.of("Some error occurred"))))
                .when(this.favouriteProductsClient).addProductToFavourites(1);

        // when
        StepVerifier.create(this.productController.addProductToFavourites(
                        Mono.just(new Product(1, "Product 1", "Description 1"))))
                // then
                .expectNext("redirect:/customer/products/1")
                .verifyComplete();

        verify(this.favouriteProductsClient).addProductToFavourites(1);
        verifyNoMoreInteractions(this.favouriteProductsClient);
        verifyNoInteractions(this.productReviewsClient, this.productsClient);
    }

    @Test
    void removeProductFromFavourites_RedirectsToProductPage() {
        // given
        doReturn(Mono.empty()).when(this.favouriteProductsClient).removeProductFromFavourites(1);

        // when
        StepVerifier.create(this.productController.deleteProductFromFavourites(
                        Mono.just(new Product(1, "Product 1", "Description 1"))))
                // then
                .expectNext("redirect:/customer/products/1")
                .verifyComplete();

        verify(this.favouriteProductsClient).removeProductFromFavourites(1);
        verifyNoMoreInteractions(this.favouriteProductsClient);
        verifyNoInteractions(this.productsClient, this.productReviewsClient);
    }

    @Test
    void createReview_RequestIsValid_RedirectsToProductPage() {
        // given
        var model = new ConcurrentModel();
        var response = new MockServerHttpResponse();

        doReturn(Mono.just(new ProductReview(UUID.fromString("86efa22c-cbae-11ee-ab01-679baf165fb7"), 1, 3, "Ну, на троечку...")))
                .when(this.productReviewsClient).createProductReview(1, 3, "Not bad");

        // when
        StepVerifier.create(this.productController.createReview(
                        Mono.just(new Product(1, "Product 1", "Description 1")),
                        new NewProductReviewPayload(3, "Not bad"), model, response))
                // then
                .expectNext("redirect:/customer/products/1")
                .verifyComplete();

        assertNull(response.getStatusCode());

        verify(this.productReviewsClient).createProductReview(1, 3, "Not bad");
        verifyNoMoreInteractions(this.productReviewsClient);
        verifyNoInteractions(this.productsClient, this.favouriteProductsClient);
    }

    @Test
    void createReview_RequestIsInvalid_ReturnsProductPageWithPayloadAndErrors() {
        // given
        var model = new ConcurrentModel();
        var response = new MockServerHttpResponse();

        var favouriteProduct = new FavouriteProduct(UUID.fromString("af5f9496-cbaa-11ee-a407-27b46917819e"), 1);
        doReturn(Mono.just(favouriteProduct)).when(this.favouriteProductsClient).findFavouriteProductByProductId(1);

        doReturn(Mono.error(new ClientBadRequestException(null, List.of("Error 1", "Error 2"))))
                .when(this.productReviewsClient).createProductReview(1, null, "Too big review");

        // when
        StepVerifier.create(this.productController.createReview(
                        Mono.just(new Product(1, "Product 1", "Description 1")),
                        new NewProductReviewPayload(null, "Too big review"), model, response))
                // then
                .expectNext("customer/products/product")
                .verifyComplete();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        assertEquals(true, model.getAttribute("inFavourite"));
        assertEquals(new NewProductReviewPayload(null, "Too big review"), model.getAttribute("payload"));
        assertEquals(List.of("Error 1", "Error 2"), model.getAttribute("errors"));

        verify(this.productReviewsClient).createProductReview(1, null, "Too big review");
        verify(this.favouriteProductsClient).findFavouriteProductByProductId(1);
        verifyNoMoreInteractions(this.productReviewsClient, this.favouriteProductsClient);
        verifyNoInteractions(this.productsClient);
    }

    @Test
    void handleNoSuchElementException_ReturnsErrors404(){
        // given
        var exception = new NoSuchElementException("Product not found");
        var model = new ConcurrentModel();
        var response = new MockServerHttpResponse();

        // when
        var result = productController.handleNoSuchElementException(exception, model, response);

        // then
        assertEquals("errors/404", result);
        assertEquals("Product not found", model.get("error"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


}