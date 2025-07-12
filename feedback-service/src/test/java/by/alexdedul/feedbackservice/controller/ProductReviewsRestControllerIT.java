package by.alexdedul.feedbackservice.controller;

import by.alexdedul.feedbackservice.entity.ProductReview;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@Slf4j
@SpringBootTest
@AutoConfigureWebTestClient
class ProductReviewsRestControllerIT {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReactiveMongoTemplate reactiveMongoTemplate;

    @BeforeEach
    void setUp() {
        reactiveMongoTemplate.insertAll(List.of(
                new ProductReview(UUID.fromString("eb019771-4fd8-4767-8e02-1c71dc27caff"), 1, 1, "Review 1", "user1"),
                new ProductReview(UUID.fromString("8d4d2378-9d90-465f-b108-5eb654d85742"), 1, 3, "Review 2", "user2"),
                new ProductReview(UUID.fromString("97ad5ec9-3248-4c89-9634-27f83e6c01ac"), 1, 5, "Review 3", "user3")
        )).blockLast();
    }

    @AfterEach
    void tearDown() {
        reactiveMongoTemplate.remove(ProductReview.class).all().block();
    }

    @Test
    void findProductReviewsByProductId_ReturnsReviews() {
        // when


        // then
        webTestClient.mutateWith(mockJwt())
                .mutate().filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
                    log.info("========= REQUEST ========");
                    log.info("Method: {} URL: {}", clientRequest.method(), clientRequest.url());
                    clientRequest.headers().forEach((key, values) -> values.forEach(value -> log.info("Key: {} Value: {}", key, value)));
                    log.info("========= END REQUEST ========");
                    return Mono.just(clientRequest);
                }))
                .build()
                .get()
                .uri("/feedback-api/product-reviews/by-product-id/1")
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json("""
                  [
                    {
                      "id": "eb019771-4fd8-4767-8e02-1c71dc27caff", 
                      "productId": 1, 
                      "rating": 1, 
                      "review": "Review 1", 
                      "userId": "user1"
                    },
                    {
                      "id": "8d4d2378-9d90-465f-b108-5eb654d85742", 
                      "productId": 1, 
                      "rating": 3, 
                      "review": "Review 2", 
                      "userId": "user2"
                    },
                    {
                      "id": "97ad5ec9-3248-4c89-9634-27f83e6c01ac", 
                      "productId": 1, 
                      "rating": 5, 
                      "review": "Review 3", 
                      "userId": "user3"
                    }
                  ]
                  """);
    }

    @Test
    void findProductReviewsByProductId_UserIsNotAuthenticated_ReturnsNotAuthenticated() {
        // when


        // then
        webTestClient
                .get()
                .uri("/feedback-api/product-reviews/by-product-id/1")
                .exchange()
                .expectStatus().isUnauthorized();
    }


    @Test
    void createProductReview_RequestIsValid_ReturnsCreateProductReview() {
        //given

        //when
        webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("user-tester")))
                .post()
                .uri("/feedback-api/product-reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                      "productId": 1, 
                      "rating": 5, 
                      "review": "Review 3"
                    }""")
                .exchange()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectStatus().isCreated()
                .expectBody()
                .json("""
                {
                  "productId": 1, 
                  "rating": 5, 
                  "review": "Review 3",
                  "userId": "user-tester"
                }
                """).jsonPath("$.id").exists();
        //then
    }

    @Test
    void createProductReview_RequestIsInvalid_ReturnsBadRequest() {
        //given

        //when
        webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("user-tester")))
                .post()
                .uri("/feedback-api/product-reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {
                      "productId": null, 
                      "rating": -1, 
                      "review": "Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3Review 3"
                    }""")
                .exchange()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectHeader().doesNotExist(HttpHeaders.LOCATION)
                .expectStatus().isBadRequest()
                .expectBody()
                .json("""
                {
                  "errors": [
                    "Product cannot be empty",
                    "Rating cannot be less than 1",
                    "Review size cannot be more than 1000 symbols"
                  ]
                }
                """);
        //then
    }

    @Test
    void createProductReview_UserIsNotAuthenticated_ReturnsNotAuthorized() {
        // given

        // when
        this.webTestClient
                .post()
                .uri("/feedback-api/product-reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": 1,
                            "rating": 5,
                            "review": "Good!"
                        }""")
                .exchange()
                // then
                .expectStatus().isUnauthorized();
    }
}