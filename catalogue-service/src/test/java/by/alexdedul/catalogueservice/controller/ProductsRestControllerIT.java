package by.alexdedul.catalogueservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class ProductsRestControllerIT {
    @Autowired
    MockMvc mockMvc;

    @Test
    @Sql("/sql/products.sql")
    void findProducts_ReturnsProductList() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue-api/products")
                .param("filter", "product")
                .with(jwt().jwt(builder ->  builder.claim("scope", "view_catalogue")));

        //when
        mockMvc.perform(requestBuilder)
        //then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json("""
                            [
                              {"id": 1, "title": "Product 1", "details": "Product description 1"},
                              {"id": 3, "title": "Product 3", "details": "Product description 3"}
                            ]
                            """)
                );


    }

    @Test
    @Sql("/sql/products.sql")
    void findProducts_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.get("/catalogue-api/products")
                .param("filter", "product")
                .with(jwt());

        //when
        mockMvc.perform(requestBuilder)
                //then
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );


    }

    @Test
    void createProduct_ReturnsIsValid_ReturnsNewProduct() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue-api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"One more product", "details":"Description for product"}
                    """)
                .with(jwt().jwt(builder ->  builder.claim("scope", "edit_catalogue")));

        //when
        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isCreated(),
                        header().string(HttpHeaders.LOCATION, "http://localhost/catalogue-api/products/1"),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json("""
                            {"id": 1, "title": "One more product", "details": "Description for product"}
                            """)
                );
    }

    @Test
    void createProduct_ReturnsIsInvalid_ReturnsProblemDetail() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue-api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"  ", "details":null}
                    """)
                .locale(Locale.ENGLISH)
                .with(jwt().jwt(builder ->  builder.claim("scope", "edit_catalogue")));

        //when
        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                            {
                              "errors": [
                                "Title cannot be empty",
                                "Length of product name should be from 3 to 50 letters"
                              ]
                            }
                            """)
                );
    }

    @Test
    void createProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        //given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue-api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"  ", "details":null}
                    """)
                .locale(Locale.ENGLISH)
                .with(jwt().jwt(builder ->  builder.claim("scope", "view_catalogue")));

        //when
        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }
}