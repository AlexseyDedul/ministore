package by.alexdedul.catalogueservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.mockito.Mockito.mock;

@Configuration
public class TestBeans {

    @Bean
    public JwtDecoder jwtDecoder() {
        return mock(JwtDecoder.class);
    }
}
