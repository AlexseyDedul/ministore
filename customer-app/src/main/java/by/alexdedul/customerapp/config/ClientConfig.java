package by.alexdedul.customerapp.config;

import by.alexdedul.customerapp.client.WebClienProductReviewsClient;
import by.alexdedul.customerapp.client.WebClientFavouriteProductsClient;
import by.alexdedul.customerapp.client.WebClientProductsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {
    @Bean
    public WebClientProductsClient webClientProductsClient(
            @Value("${ministore.services.catalogue.uri:http://localhost:8081}") String catalogueUri
    ) {
        return new WebClientProductsClient(WebClient.builder()
                .baseUrl(catalogueUri)
                .build());
    }

    @Bean
    public WebClientFavouriteProductsClient webClientFavouriteProductsClient(
            @Value("${ministore.services.feedback.uri:http://localhost:8084}") String feedbackUri
    ) {
        return new WebClientFavouriteProductsClient(WebClient.builder()
                .baseUrl(feedbackUri)
                .build());
    }

    @Bean
    public WebClienProductReviewsClient webClienProductReviewsClient(
            @Value("${ministore.services.feedback.uri:http://localhost:8084}") String feedbackUri
    ) {
        return new WebClienProductReviewsClient(WebClient.builder()
                .baseUrl(feedbackUri)
                .build());
    }
}
