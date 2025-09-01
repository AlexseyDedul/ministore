package by.alexdedul.managerapp.controller;

import by.alexdedul.managerapp.client.ProductsRestClient;
import by.alexdedul.managerapp.client.exception.BadRequestException;
import by.alexdedul.managerapp.controller.payload.NewProductPayload;
import by.alexdedul.managerapp.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ConcurrentModel;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductsControllerTest {
    @Mock
    ProductsRestClient productsRestClient;

    @InjectMocks
    ProductsController productsController;

    @Test
    void getProductsList_ReturnsProductsListPage() {
        // given
        var model = new ConcurrentModel();
        var filter = "product";

        var products = IntStream.range(1, 4)
                .mapToObj(i -> new Product(i, "Product %d".formatted(i),
                        "Description %d".formatted(i)))
                .toList();

        doReturn(products).when(this.productsRestClient).getProducts(filter);

        // when
        var result = productsController.getProductList(model, filter);

        // then
        assertEquals("catalogue/products/list", result);
        assertEquals(filter, model.getAttribute("filter"));
        assertEquals(products, model.getAttribute("products"));
    }

    @Test
    void getNewProductPage_ReturnsNewProductPage () {
        // given

        // when
        var result = productsController.getNewProductPage();

        // then
        assertEquals("catalogue/products/new_product", result);
    }

    @Test
    @DisplayName("createProduct created new product and redirect on a product`s page")
    void createProduct_RequestIsValid_ReturnsRedirectionToProductPage() {
        //given
        var payload = new NewProductPayload("Product 1", "details");
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();

        doReturn(new Product(1, "Product 1", "details"))
                .when(productsRestClient)
                .createProduct("Product 1", "details");

        //when
        var result = productsController.createProduct(payload, model, response);
        //then
        assertEquals("redirect:/catalogue/products/1", result);
        verify(productsRestClient).createProduct("Product 1", "details");
        verifyNoMoreInteractions(productsRestClient);
    }

    @Test
    @DisplayName("createProduct returns the page with errors, if the request is not valid.")
    void createProduct_RequestIsInvalid_ReturnsProductFormWithErrors() {
        //given
        var payload = new NewProductPayload("  ", null);
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();

        doThrow(new BadRequestException(List.of("error", "error2")))
                .when(productsRestClient)
                .createProduct("  ", null);

        //when
        var result = productsController.createProduct(payload, model, response);
        //then
        assertEquals("catalogue/products/new_product", result);
        assertEquals(payload, model.getAttribute("payload"));
        assertEquals(List.of("error", "error2"), model.getAttribute("errors"));

        verify(productsRestClient).createProduct("  ", null);
        verifyNoMoreInteractions(productsRestClient);
    }
}