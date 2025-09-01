package by.alexdedul.managerapp.config;

import by.alexdedul.managerapp.client.RestClientProductsRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.client.RestClient;

import static org.mockito.Mockito.mock;

@Configuration
public class TestingBeans {

    @Bean
    public JwtDecoder jwtDecoder() {
        return mock(JwtDecoder.class);
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return mock(ClientRegistrationRepository.class);
    }

    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository() {
        return mock(OAuth2AuthorizedClientRepository.class);
    }

    @Bean(name = "testRestClientProductsRestClient")
    @Primary
    public RestClientProductsRestClient restClientProductsRestClient(
            @Value("${ministore.services.catalogue.uri:http://localhost:54321}") String catalogueBaseUri
    ) {
        var httpClient = java.net.http.HttpClient.newBuilder()
                .version(java.net.http.HttpClient.Version.HTTP_1_1) // ← ключевая строка
                .build();

        return new RestClientProductsRestClient(RestClient.builder()
                .baseUrl(catalogueBaseUri)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build());
    }
}
