package by.alexdedul.managerapp.controller;

import by.alexdedul.managerapp.controller.payload.NewProductPayload;
import by.alexdedul.managerapp.entity.Product;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WireMockTest(httpPort = 54321)
class ProductsControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getProductsList_ReturnsProductsListPage() throws Exception {
        //given
        var request = MockMvcRequestBuilders.get("/catalogue/products/list")
                .queryParam("filter", "product")
                .with(user("alexdedul").roles("MANAGER"));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", WireMock.equalTo("product"))
                .willReturn(WireMock.ok(
                        """
                                [
                                  {"id": 1, "title": "Product 1", "details": "Description 1"},
                                  {"id": 2, "title": "Product 2", "details": "Description 2"}
                                ]
                                """
                ).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
        //then
        mockMvc.perform(request)
                //then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/products/list"),
                        model().attribute("filter", "product"),
                        model().attribute("products", List.of(
                                new Product(1, "Product 1", "Description 1"),
                                new Product(2, "Product 2", "Description 2")
                        ))
                );

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching("/catalogue-api/products"))
                .withQueryParam("filter", WireMock.equalTo("product")));
    }

    @Test
    void getProductList_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/list")
                .queryParam("filter", "product")
                .with(user("alexdedul"));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    void getNewProductPage_ReturnsProductPage() throws Exception {
        //given
        var RequestBuilder = MockMvcRequestBuilders.get("/catalogue/products/create")
                .with(user("alexdedul").roles("MANAGER"));
        //when
        mockMvc.perform(RequestBuilder)
        //then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("catalogue/products/new_product")
                );

    }

    @Test
    void getNewProductPage_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue/products/create")
                .with(user("alexdedul"));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    void createProduct_RequestIsValid_RedirectsToProductPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/create")
                .param("title", "New product")
                .param("details", "New description")
                .with(user("alexdedul").roles("MANAGER"))
                .with(csrf());

        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/catalogue-api/products"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "New product",
                            "details": "New description"
                        }"""))
                .willReturn(WireMock.created()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "id": 1,
                                    "title": "New product",
                                    "details": "New description"
                                }""")));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().is3xxRedirection(),
                        header().string(HttpHeaders.LOCATION, "/catalogue/products/1")
                );

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlPathMatching("/catalogue-api/products"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "New product",
                            "details": "New description"
                        }""")));
    }

    @Test
    void createProduct_RequestIsInvalid_ReturnsNewProductPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/create")
                .param("title", "cc")
                .with(user("alexdedul").roles("MANAGER"))
                .with(csrf());

        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/catalogue-api/products"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "cc",
                            "details": null
                        }"""))
                .willReturn(WireMock.badRequest()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                        .withBody("""
                                {
                                    "errors": ["Error 1", "Error 2"]
                                }""")));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        view().name("catalogue/products/new_product"),
                        model().attribute("payload", new NewProductPayload("cc", null)),
                        model().attribute("errors", List.of("Error 1", "Error 2"))
                );

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlPathMatching("/catalogue-api/products"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "cc",
                            "details": null
                        }""")));
    }

    @Test
    void createProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/create")
                .param("title", "New product")
                .param("details", "New  description")
                .with(user("alexdedul"))
                .with(csrf());

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }
}
