package by.alexdedul.catalogueservice.repository;

import by.alexdedul.catalogueservice.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql("/sql/products.sql")
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryIT {
    @Autowired
    private ProductRepository productRepository;

    @Test
    void findAllByTitleLikeIgnoreCase_ReturnsFilteredProductsList() {
        //given
        var filter = "%product%";

        //when
        var products = productRepository.findAllByTitleLikeIgnoreCase(filter);

        //then
        assertEquals(List.of(
                new Product(1, "Product 1", "Product description 1"),
                new Product(3, "Product 3", "Product description 3")
        ), products);

    }
}