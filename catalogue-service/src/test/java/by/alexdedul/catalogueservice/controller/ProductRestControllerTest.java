package by.alexdedul.catalogueservice.controller;

import by.alexdedul.catalogueservice.controller.payload.UpdateProductPayload;
import by.alexdedul.catalogueservice.entity.Product;
import by.alexdedul.catalogueservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductRestControllerTest {
    @Mock
    ProductService productService;

    @Mock
    MessageSource messageSource;

    @InjectMocks
    ProductRestController controller;

    @Test
    void getProduct_ProductExists_ReturnsProduct() {
        // given
        var product = new Product(1, "Product Title", "Description");
        doReturn(Optional.of(product)).when(productService).findProductById(1);

        // when
        var result = this.controller.getProduct(1);

        // then
        assertEquals(product, result);
    }

    @Test
    void getProduct_ProductDoesNotExist_ThrowsNoSuchElementException() {
        // given

        // when
        var exception = assertThrows(NoSuchElementException.class, () -> this.controller.getProduct(1));

        // then
        assertEquals("catalogue.errors.product.not_found", exception.getMessage());
    }

    @Test
    void findProduct_ReturnsProduct() {
        // given
        var product = new Product(1, "Product Title", "Description");

        // when
        var result = this.controller.findProductById(product);

        // then
        assertEquals(product, result);
    }

    @Test
    void updateProduct_RequestIsValid_ReturnsNoContent() throws BindException {
        // given
        var payload = new UpdateProductPayload("Product Title", "Description");
        var bindingResult = new MapBindingResult(Map.of(), "payload");

        // when
        var result = this.controller.updateProduct(1, payload, bindingResult);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        verify(this.productService).updateProduct(1, "Product Title", "Description");
    }

    @Test
    void updateProduct_RequestIsInvalid_ReturnsBadRequest() {
        // given
        var payload = new UpdateProductPayload("   ", null);
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        bindingResult.addError(new FieldError("payload", "title", "error"));

        // when
        var exception = assertThrows(BindException.class, () -> this.controller.updateProduct(1, payload, bindingResult));

        // then
        assertEquals(List.of(new FieldError("payload", "title", "error")), exception.getAllErrors());
        verifyNoInteractions(this.productService);
    }

    @Test
    void updateProduct_RequestIsInvalidAndBindResultIsBindException_ReturnsBadRequest() {
        // given
        var payload = new UpdateProductPayload("   ", null);
        var bindingResult = new BindException(new MapBindingResult(Map.of(), "payload"));
        bindingResult.addError(new FieldError("payload", "title", "error"));

        // when
        var exception = assertThrows(BindException.class, () -> this.controller.updateProduct(1, payload, bindingResult));

        // then
        assertEquals(List.of(new FieldError("payload", "title", "error")), exception.getAllErrors());
        verifyNoInteractions(this.productService);
    }

    @Test
    void deleteProduct_ReturnsNoContent() {
        // given

        // when
        var result = this.controller.deleteProduct(1);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        verify(this.productService).deleteProduct(1);
    }

    @Test
    void handleNoSuchElementException_ReturnsNotFound() {
        // given
        var exception = new NoSuchElementException("error_code");
        var locale = Locale.ENGLISH;

        doReturn("error details").when(this.messageSource)
                .getMessage("error_code", new Object[0], "error_code", Locale.ENGLISH);

        // when
        var result = this.controller.handleNoSuchElementException(exception, locale);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertInstanceOf(ProblemDetail.class, result.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getBody().getStatus());
        assertEquals("error details", result.getBody().getDetail());

        verifyNoInteractions(this.productService);
    }
}