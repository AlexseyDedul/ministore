package by.alexdedul.managerapp.config;

import by.alexdedul.managerapp.client.RestClientProductsRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientBeans {
    @Bean
    RestClientProductsRestClient restClientProductsRestClient(
            @Value("${ministore.services.catalogue.uri:http://localhost:8081}") String catalogueBaseUri,
            @Value("${ministore.services.catalogue.username:}") String catalogueUsername,
            @Value("${ministore.services.catalogue.password:}") String cataloguePassword
    ) {
        return new RestClientProductsRestClient(RestClient.builder()
                .baseUrl(catalogueBaseUri)
                .requestInterceptor(new BasicAuthenticationInterceptor(catalogueUsername, cataloguePassword))
                .build());
    }
}
