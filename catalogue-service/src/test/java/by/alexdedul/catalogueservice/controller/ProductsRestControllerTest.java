package by.alexdedul.catalogueservice.controller;

import by.alexdedul.catalogueservice.controller.payload.NewProductPayload;
import by.alexdedul.catalogueservice.entity.Product;
import by.alexdedul.catalogueservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductsRestControllerTest {

    @Mock
    private ProductService productService;
    @InjectMocks
    private ProductsRestController productsRestController;

    @Test
    void findProduct_ReturnsProductsList() {
        //given
        var filter = "product";

        doReturn(List.of(
                new Product(1, "Product 1", "Description"),
                new Product(2, "Product 2", "Description")
        )).when(productService).findAllProducts(filter);

        //when
        var result = productsRestController.findProducts(filter);

        //then
        assertEquals(List.of(
                new Product(1, "Product 1", "Description"),
                new Product(2, "Product 2", "Description")), result);
    }

    @Test
    void createProduct_RequestIsValid_ReturnsNoContent() throws BindException {
        //given
        var payload = new NewProductPayload("New Product", "Description");
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        var uriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost");

        doReturn(new Product(1, "New Product", "Description"))
                .when(productService).createProduct("New Product", "Description");

        //when
        var result = productsRestController.createProduct(payload, bindingResult, uriComponentsBuilder);

        //then
        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(URI.create("http://localhost/catalogue-api/products/1"), result.getHeaders().getLocation());
        assertEquals(new Product(1, "New Product", "Description"), result.getBody());

        verify(this.productService).createProduct("New Product", "Description");
        verifyNoMoreInteractions(this.productService);
    }

    @Test
    void createProduct_RequestIsInvalid_ReturnsBadRequest() {
        // given
        var payload = new NewProductPayload("   ", null);
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        bindingResult.addError(new FieldError("payload", "title", "error"));
        var uriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost");

        // when
        var exception = assertThrows(BindException.class,
                () -> productsRestController.createProduct(payload, bindingResult, uriComponentsBuilder));

        // then
        assertEquals(List.of(new FieldError("payload", "title", "error")), exception.getAllErrors());
        verifyNoInteractions(this.productService);
    }

    @Test
    void createProduct_RequestIsInvalidAndBindResultIsBindException_ReturnsBadRequest() {
        // given
        var payload = new NewProductPayload("   ", null);
        var bindingResult = new BindException(new MapBindingResult(Map.of(), "payload"));
        bindingResult.addError(new FieldError("payload", "title", "error"));
        var uriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost");

        // when
        var exception = assertThrows(BindException.class,
                () -> productsRestController.createProduct(payload, bindingResult, uriComponentsBuilder));

        // then
        assertEquals(List.of(new FieldError("payload", "title", "error")), exception.getAllErrors());
        verifyNoInteractions(this.productService);
    }
}