package by.alexdedul.customerapp.config;

import jakarta.annotation.Priority;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
public class SecurityConfig {

    @Bean
    @Priority(0)
    public SecurityWebFilterChain metricsSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(configurer -> configurer
                        .pathMatchers("/actuator/**").hasAuthority("SCOPE_metrics")
                        .anyExchange().authenticated())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .oauth2ResourceServer(customizer -> customizer.jwt(Customizer.withDefaults()))
                .oauth2Client(Customizer.withDefaults())
                .build();
    }

    @Bean
    @Priority(1)
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(configurer -> configurer.anyExchange().authenticated())
                .oauth2Login(Customizer.withDefaults())
                .oauth2Client(Customizer.withDefaults())
                .build();
    }
}
