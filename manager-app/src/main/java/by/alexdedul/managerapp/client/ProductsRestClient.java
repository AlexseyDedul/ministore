package by.alexdedul.managerapp.client;

import by.alexdedul.managerapp.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductsRestClient {
    List<Product> getProducts(String filter);
    Product createProduct(String title, String details);
    Optional<Product> getProduct(int productId);
    void updateProduct(int productId, String title, String details);
    void deleteProduct(int productId);
}
