package by.alexdedul.managerapp.config;

import org.springframework.cloud.netflix.eureka.RestClientTimeoutProperties;
import org.springframework.cloud.netflix.eureka.http.DefaultEurekaClientHttpRequestFactorySupplier;
import org.springframework.cloud.netflix.eureka.http.EurekaClientHttpRequestFactorySupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.List;
import java.util.Set;

@Configuration
public class DiscoveryBeans {

    @Bean
    public DefaultEurekaClientHttpRequestFactorySupplier defaultEurekaClientHttpRequestFactorySupplier(
            RestClientTimeoutProperties restClientTimeoutProperties,
            EurekaClientHttpRequestFactorySupplier.RequestConfigCustomizer requestConfigCustomizer,
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService
    ){
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository,  authorizedClientService);

        return new DefaultEurekaClientHttpRequestFactorySupplier(
                restClientTimeoutProperties,
                Set.of(requestConfigCustomizer),
                List.of((request, entity, context) -> {
                    if(!request.containsHeader(HttpHeaders.AUTHORIZATION)){
                        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(OAuth2AuthorizeRequest
                                    .withClientRegistrationId("discovery")
                                    .principal("manager-app")
                                    .build());
                        request.setHeader(HttpHeaders.AUTHORIZATION,
                                "Bearer %s".formatted(authorizedClient.getAccessToken().getTokenValue()));
                    }
                }));
    }

    @Bean
    public EurekaClientHttpRequestFactorySupplier.RequestConfigCustomizer requestConfigCustomizer() {
        return builder -> builder.setProtocolUpgradeEnabled(false);
    }
}
