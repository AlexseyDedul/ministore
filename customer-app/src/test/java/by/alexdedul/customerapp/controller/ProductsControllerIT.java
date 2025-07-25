package by.alexdedul.customerapp.controller;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

@SpringBootTest
@AutoConfigureWebTestClient
@WireMockTest(httpPort = 54321)
public class ProductsControllerIT {
    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        stubFor(get(urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", equalTo("filter"))
                .willReturn(okJson("""
                        [
                            {
                                "id": 1,
                                "title": "Filtered product 1",
                                "details": "Description 1"
                            },
                            {
                                "id": 2,
                                "title": "Filtered product 2",
                                "details": "Description 2"
                            },
                            {
                                "id": 3,
                                "title": "Filtered product 3",
                                "details": "Description 3"
                            }
                        ]""")));
    }

    @Test
    void getProductsListPage_ReturnsProductsPage() {
        // given

        // when
        this.webTestClient
                .mutateWith(mockUser())
                .get()
                .uri("/customer/products/list?filter=filter")
                .exchange()
                // then
                .expectStatus().isOk();

        verify(getRequestedFor(urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", equalTo("filter")));
    }

    @Test
    void getProductsListPage_UserIsNotAuthenticated_RedirectsToLoginPage() {
        // given

        // when
        this.webTestClient
                .get()
                .uri("/customer/products/list")
                .exchange()
                // then
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");
    }

    @Test
    void getFavouriteProductsPage_ReturnsFavouriteProductsPage() {
        // given
        stubFor(get("/feedback-api/favourite-products")
                .willReturn(okJson("""
                        [
                            {
                                "id": "a16f0218-cbaf-11ee-9e6c-6b0fa3631587",
                                "productId": 1,
                                "userId": "2051e72a-cbca-11ee-8e8b-a3841adf45d0"
                            },
                            {
                                "id": "a42ff37c-cbaf-11ee-8b1d-cb00912914b5",
                                "productId": 3,
                                "userId": "2051e72a-cbca-11ee-8e8b-a3841adf45d0"
                            }
                        ]""")
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        // when
        this.webTestClient
                .mutateWith(mockUser())
                .get()
                .uri("/customer/products/favourites?filter=filter")
                .exchange()
                // then
                .expectStatus().isOk();

        verify(getRequestedFor(urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", equalTo("filter")));
        verify(getRequestedFor(urlPathMatching("/feedback-api/favourite-products")));
    }

    @Test
    void getFavouriteProductsPage_UserIsNotAuthenticated_RedirectsToLoginPage() {
        // given

        // when
        this.webTestClient
                .get()
                .uri("/customer/products/favourites")
                .exchange()
                // then
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");
    }
}
