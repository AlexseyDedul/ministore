package by.alexdedul.customerapp.config;

import by.alexdedul.customerapp.client.WebClienProductReviewsClient;
import by.alexdedul.customerapp.client.WebClientFavouriteProductsClient;
import by.alexdedul.customerapp.client.WebClientProductsClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.web.reactive.function.client.WebClient;

import static org.mockito.Mockito.mock;

@Configuration
public class TestBeans {

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return mock(ReactiveJwtDecoder.class);
    }

    @Bean
    public ReactiveClientRegistrationRepository clientRegistrationRepository(){
        return mock(ReactiveClientRegistrationRepository.class);
    }

    @Bean
    public ServerOAuth2AuthorizedClientRepository auth2AuthorizedClientRepository(){
        return mock(ServerOAuth2AuthorizedClientRepository.class);
    }

    @Bean
    @Primary
    public WebClientProductsClient mockWebClientProductsClient() {
        return new WebClientProductsClient(WebClient.builder()
                .baseUrl("http://localhost:54321")
                .build());
    }

    @Bean
    @Primary
    public WebClientFavouriteProductsClient mockWebClientFavouriteProductsClient() {
        return new WebClientFavouriteProductsClient(WebClient.builder()
                .baseUrl("http://localhost:54321")
                .build());
    }

    @Bean
    @Primary
    public WebClienProductReviewsClient mockWebClienProductReviewsClient() {
        return new WebClienProductReviewsClient(WebClient.builder()
                .baseUrl("http://localhost:54321")
                .build());
    }
}
