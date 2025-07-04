package by.alexdedul.managerapp.controller;

import by.alexdedul.managerapp.client.ProductsRestClient;
import by.alexdedul.managerapp.client.exception.BadRequestException;
import by.alexdedul.managerapp.controller.payload.UpdateProductPayload;
import by.alexdedul.managerapp.entity.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ConcurrentModel;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {
    @Mock
    ProductsRestClient productsRestClient;

    @Mock
    MessageSource messageSource;

    @InjectMocks
    ProductController controller;

    @Test
    void product_ProductExists_ReturnsProduct() {
        // given
        var product = new Product(1, "Product 1", "Description 1");

        doReturn(Optional.of(product)).when(this.productsRestClient).getProduct(1);

        // when
        var result = this.controller.getProductById(1);

        // then
        assertEquals(product, result);

        verify(this.productsRestClient).getProduct(1);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void product_ProductDoesNotExist_ThrowsNoSuchElementException() {
        // given

        // when
        var exception = assertThrows(NoSuchElementException.class, () -> this.controller.getProductById(1));

        // then
        assertEquals("catalogue.errors.product.not_found", exception.getMessage());

        verify(this.productsRestClient).getProduct(1);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void getProduct_ReturnsProductPage() {
        // given

        // when
        var result = this.controller.getProduct();

        // then
        assertEquals("catalogue/products/product", result);

        verifyNoInteractions(this.productsRestClient);
    }

    @Test
    void getProductEditPage_ReturnsProductEditPage() {
        // given

        // when
        var result = this.controller.editProductPage();

        // then
        assertEquals("catalogue/products/edit", result);

        verifyNoInteractions(this.productsRestClient);
    }

    @Test
    void updateProduct_RequestIsValid_RedirectsToProductPage() {
        // given
        var product = new Product(1, "Product 1", "Description 1");
        var payload = new UpdateProductPayload("New title", "new description");
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();

        // when
        var result = this.controller.updateProduct(product, payload, model, response);

        // then
        assertEquals("redirect:/catalogue/products/1", result);

        verify(this.productsRestClient).updateProduct(1, "New title", "new description");
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void updateProduct_RequestIsInvalid_ReturnsProductEditPage() {
        // given
        var product = new Product(1, "New title", "new description");
        var payload = new UpdateProductPayload("   ", null);
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();

        doThrow(new BadRequestException(List.of("Error 1", "Error 2")))
                .when(this.productsRestClient).updateProduct(1, "   ", null);

        // when
        var result = this.controller.updateProduct(product, payload, model, response);

        // then
        assertEquals("catalogue/products/edit", result);
        assertEquals(payload, model.getAttribute("payload"));
        assertEquals(List.of("Error 1", "Error 2"), model.getAttribute("errors"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

        verify(this.productsRestClient).updateProduct(1, "   ", null);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void deleteProduct_RedirectsToProductsListPage() {
        // given
        var product = new Product(1, "New title", "new description");

        // when
        var result = this.controller.deleteProduct(product);

        // then
        assertEquals("redirect:/catalogue/products/list", result);

        verify(this.productsRestClient).deleteProduct(1);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void handleNoSuchElementException_Returns404ErrorPage() {
        // given
        var exception = new NoSuchElementException("error");
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();
        var locale = Locale.ENGLISH;

        doReturn("Error").when(this.messageSource)
                .getMessage("error", new Object[0], "error", Locale.ENGLISH);

        // when
        var result = this.controller.handleNotFound(exception, model, response, locale);

        // then
        assertEquals("errors/404",  result);
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

        verify(this.messageSource).getMessage("error", new Object[0], "error", Locale.ENGLISH);
        verifyNoMoreInteractions(this.messageSource);
        verifyNoInteractions(this.productsRestClient);
    }
}
