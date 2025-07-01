package by.alexdedul.managerapp.config;

import by.alexdedul.managerapp.client.RestClientProductsRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientBeans {
    @Bean
    RestClientProductsRestClient restClientProductsRestClient(
            @Value("${ministore.services.catalogue.uri:http://localhost:8081}") String catalogueBaseUri
    ) {
        return new RestClientProductsRestClient(RestClient.builder()
                .baseUrl(catalogueBaseUri)
                .build());
    }
}
