package by.alexdedul.managerapp.client;

import by.alexdedul.managerapp.client.exception.BadRequestException;
import by.alexdedul.managerapp.controller.payload.NewProductPayload;
import by.alexdedul.managerapp.controller.payload.UpdateProductPayload;
import by.alexdedul.managerapp.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RequiredArgsConstructor
public class RestClientProductsRestClient implements ProductsRestClient {
    private static final ParameterizedTypeReference<List<Product>> PRODUCT_LIST_TYPE =
            new ParameterizedTypeReference<List<Product>>() {};

    private final RestClient restClient;

    @Override
    public List<Product> getProducts() {
        return restClient.get()
                .uri("/catalogue-api/products")
                .retrieve()
                .body(PRODUCT_LIST_TYPE);
    }

    @Override
    public Product createProduct(String title, String details) {
        try {
            return restClient.post()
                    .uri("/catalogue-api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new NewProductPayload(title, details))
                    .retrieve()
                    .body(Product.class);
        }catch (HttpClientErrorException.BadRequest e) {
            ProblemDetail problem = e.getResponseBodyAs(ProblemDetail.class);
            throw new BadRequestException((List<String>) problem.getProperties().get("errors"));
        }
    }

    @Override
    public Optional<Product> getProduct(int productId) {
        try {
            return Optional.ofNullable(restClient.get()
                    .uri("/catalogue-api/products/{productId}", productId)
                    .retrieve()
                    .body(Product.class));
        }catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    @Override
    public void updateProduct(int productId, String title, String details) {
        try {
            restClient.patch()
                    .uri("/catalogue-api/products/{productId}", productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new UpdateProductPayload(title, details))
                    .retrieve()
                    .toBodilessEntity();
        }catch (HttpClientErrorException.BadRequest e) {
            ProblemDetail problem = e.getResponseBodyAs(ProblemDetail.class);
            throw new BadRequestException((List<String>) problem.getProperties().get("errors"));
        }
    }

    @Override
    public void deleteProduct(int productId) {
        try {
            Optional.ofNullable(restClient.delete()
                    .uri("/catalogue-api/products/{productId}", productId)
                    .retrieve()
                    .toBodilessEntity());
        }catch (HttpClientErrorException.NotFound e) {
            throw new NoSuchElementException(e);
        }
    }
}
