package by.alexdedul.customerapp.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

@SpringBootTest
@AutoConfigureWebTestClient
@WireMockTest(httpPort = 54321)
class ProductControllerIT {
    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        stubFor(get("/catalogue-api/products/1")
                .willReturn(okJson("""
                        {
                            "id": 1,
                            "title": "Product number 1",
                            "details": "Description of product 1"
                        }""")
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
    }

    @Test
    void getProductPage_ProductExists_ReturnsProductPage() {
        // given
        stubFor(get("/feedback-api/product-reviews/by-product-id/1")
                .willReturn(okJson("""
                        [
                            {
                                "id": "595d4e5a-cbc1-11ee-864f-8fb72674ccaf",
                                "productId": 1,
                                "rating": 3,
                                "review": "Not bad",
                                "userId": "5da9bf2a-cbc1-11ee-a8a7-d355f5a3dd8e"
                            },
                            {
                                "id": "63c4410a-cbc1-11ee-92ea-eff590e7852e",
                                "productId": 1,
                                "rating": 5,
                                "review": "Perfect",
                                "userId": "6b3cce0c-cbc1-11ee-ac61-b7eed6e7b4f4"
                            }
                        ]""")
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        stubFor(get("/feedback-api/favourite-products/by-product-id/1")
                .willReturn(created()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "id": "2ecc74c2-cb17-11ee-b719-e35a0e241f11",
                                    "productId": 1
                                }""")));

        // when
        this.webTestClient
                .mutateWith(mockUser())
                .get()
                .uri("/customer/products/1")
                .exchange()
                // then
                .expectStatus().isOk();

        verify(getRequestedFor(urlPathMatching("/catalogue-api/products/1")));
        verify(getRequestedFor(urlPathMatching("/feedback-api/product-reviews/by-product-id/1")));
        verify(getRequestedFor(urlPathMatching("/feedback-api/favourite-products/by-product-id/1")));

    }

    @Test
    void getProductPage_ProductDoesNotExist_ReturnsNotFound() {
        // given

        // when
        this.webTestClient
                .mutateWith(mockUser())
                .get()
                .uri("/customer/products/404")
                .exchange()
                // then
                .expectStatus().isNotFound();

        verify(getRequestedFor(urlPathMatching("/catalogue-api/products/404")));
    }

    @Test
    void getProductPage_UserIsNotAuthorized_RedirectsToLoginPage() {
        // given

        // when
        this.webTestClient
                .get()
                .uri("/customer/products/1")
                .exchange()
                // then
                .expectStatus().isFound()
                .expectHeader().location("/login");
    }

    @Test
    void addProductToFavourites_RequestIsValid_ReturnsRedirectionToProductPage() {
        //given
        WireMock.stubFor(WireMock.get("/catalogue-api/products/1")
                .willReturn(WireMock.okJson("""
                      {
                        "id": 1,
                        "title": "Product 1",
                        "details": "Description 1"
                      }""")
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                ));

        WireMock.stubFor(WireMock.post("/feedback-api/favourite-products")
                .withRequestBody(WireMock.equalToJson("""
                      {
                        "productId": 1
                      }"""))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(WireMock.created()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "id": "d62bcb6a-bd0f-4316-9180-05453fa57058",
                                    "productId": 1
                                }""")));

        //when
        webTestClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/add-to-favourites")
                //then
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/customer/products/1");

        WireMock.verify(getRequestedFor(urlPathMatching("/catalogue-api/products/1")));
        WireMock.verify(postRequestedFor(urlPathMatching("/feedback-api/favourite-products"))
                .withRequestBody(equalToJson("""
                      {
                        "productId": 1
                      }""")));
    }

    @Test
    void addProductToFavourites_RequestDoesNotExist_ReturnsNotFoundPage() {
        //given

        //when
        webTestClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1400/add-to-favourites")
                //then
                .exchange()
                .expectStatus().isNotFound();

        WireMock.verify(getRequestedFor(urlPathMatching("/catalogue-api/products/1400")));
    }

    @Test
    void addProductToFavourites_UserIsNotAuthorized_RedirectsToLoginPage() {
        // given

        // when
        this.webTestClient
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/add-to-favourites")
                .exchange()
                // then
                .expectStatus().isFound()
                .expectHeader().location("/login");
    }

    @Test
    void removeProductFromFavourites_ProductExists_ReturnsRedirectionToProductPage() {
        // given
        stubFor(delete("/feedback-api/favourite-products/by-product-id/1")
                .willReturn(noContent()));

        // when
        this.webTestClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/remove-from-favourites")
                .exchange()
                // then
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/customer/products/1");

        verify(getRequestedFor(urlPathMatching("/catalogue-api/products/1")));
        verify(deleteRequestedFor(urlPathMatching("/feedback-api/favourite-products/by-product-id/1")));
    }

    @Test
    void removeProductFromFavourites_ProductDoesNotExist_ReturnsNotFoundPage() {
        // given

        // when
        this.webTestClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/404/remove-from-favourites")
                .exchange()
                // then
                .expectStatus().isNotFound();

        verify(getRequestedFor(urlPathMatching("/catalogue-api/products/404")));
    }

    @Test
    void removeProductFromFavourites_UserIsNotAuthorized_RedirectsToLoginPage() {
        // given

        // when
        this.webTestClient
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/remove-from-favourites")
                .exchange()
                // then
                .expectStatus().isFound()
                .expectHeader().location("/login");
    }

    @Test
    void createReview_RequestIsValid_RedirectsToProductPage() {
        // given
        stubFor(post("/feedback-api/product-reviews")
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson("""
                        {
                            "productId": 1,
                            "rating": 3,
                            "review": "Not bad"
                        }"""))
                .willReturn(created()
                        .withHeader(HttpHeaders.LOCATION, "http://localhost/feedback-api/product-reviews/b852bc8e-cbc5-11ee-bbc5-bf192e2492e5")
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "id": "b852bc8e-cbc5-11ee-bbc5-bf192e2492e5",
                                    "productId": 1,
                                    "rating": 3,
                                    "review": "Not bad",
                                    "userId": "1a24d4ec-cbc6-11ee-af3b-0b236022162c"
                                }
                               """)));

        // when
        this.webTestClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/create-review")
                .body(BodyInserters.fromFormData("rating", "3")
                        .with("review", "Not bad"))
                // then
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/customer/products/1");

        verify(postRequestedFor(urlPathMatching("/feedback-api/product-reviews"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson("""
                        {
                            "productId": 1,
                            "rating": 3,
                            "review": "Not bad"
                        }""")));
    }


    @Test
    void createReview_RequestIsInvalid_ReturnsProductPage() throws Exception {
        // given
        stubFor(post("/feedback-api/product-reviews")
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson("""
                        {
                            "productId": 1,
                            "rating": -1,
                            "review": "Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review "
                        }"""))
                .willReturn(badRequest()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                        .withBody("""
                                {
                                    "errors": ["Error 1", "Error 2"]
                                }""")));

        stubFor(get("/feedback-api/favourite-products/by-product-id/1")
                .willReturn(okJson("""
                        {
                            "id": "ec586ecc-cbc8-11ee-8e7d-4fce5e860855",
                            "productId": 1,
                            "userId": "f1177a8e-cbc8-11ee-8ca2-0bf025125fd5"
                        }""")
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        // when
        this.webTestClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/create-review")
                .body(BodyInserters.fromFormData("rating", "-1")
                        .with("review", "Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review "))
                // then
                .exchange()
                .expectStatus().isBadRequest();

        verify(postRequestedFor(urlPathMatching("/feedback-api/product-reviews"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson("""
                        {
                            "productId": 1,
                            "rating": -1,
                            "review": "Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review Too many big review "
                        }""")));
    }

    @Test
    void createReview_ProductDoesNotExist_ReturnsNotFoundPage() {
        // given

        // when
        this.webTestClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/40411/create-review")
                .body(BodyInserters.fromFormData("rating", "3")
                        .with("review", "Not bad"))
                .exchange()
                // then
                .expectStatus().isNotFound();

        verify(getRequestedFor(urlPathMatching("/catalogue-api/products/40411")));
    }

    @Test
    void createReview_UserIsNotAuthorized_RedirectsToLoginPage() {
        // given

        // when
        this.webTestClient
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/create-review")
                .body(BodyInserters.fromFormData("rating", "3")
                        .with("review", "Not bad"))
                .exchange()
                // then
                .expectStatus().isFound()
                .expectHeader().location("/login");
    }

}