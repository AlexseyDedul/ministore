package by.alexdedul.customerapp.config;

import by.alexdedul.customerapp.client.WebClienProductReviewsClient;
import by.alexdedul.customerapp.client.WebClientFavouriteProductsClient;
import by.alexdedul.customerapp.client.WebClientProductsClient;
import de.codecentric.boot.admin.client.config.ClientProperties;
import de.codecentric.boot.admin.client.registration.ReactiveRegistrationClient;
import de.codecentric.boot.admin.client.registration.RegistrationClient;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.DefaultClientRequestObservationConvention;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.boot.admin.client.enabled", havingValue = "true")
    public RegistrationClient registrationClient(
            ClientProperties clientProperties,
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ReactiveOAuth2AuthorizedClientService authorizedClientRepository
    ) {

        ServerOAuth2AuthorizedClientExchangeFilterFunction filter =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                        new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                                clientRegistrationRepository, authorizedClientRepository));

        filter.setDefaultClientRegistrationId("metrics");

        return new ReactiveRegistrationClient(WebClient.builder()
                .filter(filter)
                .build(), clientProperties.getReadTimeout());
    }

    @Bean
    @LoadBalanced
    @Scope("prototype")
    public WebClient.Builder ministoreServiceWebClientBuilder(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ServerOAuth2AuthorizedClientRepository auth2AuthorizedClientRepository,
            ObservationRegistry observationRegistry
    ) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction filter = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                clientRegistrationRepository,
                auth2AuthorizedClientRepository);
        filter.setDefaultClientRegistrationId("keycloak");
        return WebClient.builder()
                .observationRegistry(observationRegistry)
                .observationConvention(new DefaultClientRequestObservationConvention())
                .filter(filter);
    }

    @Bean
    public WebClientProductsClient webClientProductsClient(
            @Value("${ministore.services.catalogue.uri:http://localhost:8081}") String catalogueUri,
            WebClient.Builder ministoreServiceWebClientBuilder
    ) {
        return new WebClientProductsClient(ministoreServiceWebClientBuilder
                .baseUrl(catalogueUri)
                .build());
    }

    @Bean
    public WebClientFavouriteProductsClient webClientFavouriteProductsClient(
            @Value("${ministore.services.feedback.uri:http://localhost:8084}") String feedbackUri,
            WebClient.Builder ministoreServiceWebClientBuilder
    ) {
        return new WebClientFavouriteProductsClient(ministoreServiceWebClientBuilder
                .baseUrl(feedbackUri)
                .build());
    }

    @Bean
    public WebClienProductReviewsClient webClienProductReviewsClient(
            @Value("${ministore.services.feedback.uri:http://localhost:8084}") String feedbackUri,
            WebClient.Builder ministoreServiceWebClientBuilder
    ) {
        return new WebClienProductReviewsClient(ministoreServiceWebClientBuilder
                .baseUrl(feedbackUri)
                .build());
    }
}
