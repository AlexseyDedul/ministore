//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.cloud.netflix.eureka.http;

import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.cloud.netflix.eureka.TimeoutProperties;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;

public class DefaultEurekaClientHttpRequestFactorySupplier implements EurekaClientHttpRequestFactorySupplier {
    private final TimeoutProperties timeoutProperties;
    private Set<EurekaClientHttpRequestFactorySupplier.RequestConfigCustomizer> requestConfigCustomizers = Collections.emptySet();

    private final List<HttpRequestInterceptor> requestInterceptors;

    public DefaultEurekaClientHttpRequestFactorySupplier(TimeoutProperties timeoutProperties, Set<EurekaClientHttpRequestFactorySupplier.RequestConfigCustomizer> requestConfigCustomizers) {
        this.timeoutProperties = timeoutProperties;
        this.requestConfigCustomizers = requestConfigCustomizers;
        this.requestInterceptors = List.of();
    }

    public DefaultEurekaClientHttpRequestFactorySupplier(TimeoutProperties timeoutProperties,
                                                         Set<EurekaClientHttpRequestFactorySupplier.RequestConfigCustomizer> requestConfigCustomizers,
                                                         List<HttpRequestInterceptor> requestInterceptors) {
        this.timeoutProperties = timeoutProperties;
        this.requestConfigCustomizers = requestConfigCustomizers;
        this.requestInterceptors = Collections.unmodifiableList(requestInterceptors);
    }

    public ClientHttpRequestFactory get(SSLContext sslContext, @Nullable HostnameVerifier hostnameVerifier) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        if (sslContext != null || hostnameVerifier != null || this.timeoutProperties != null) {
            httpClientBuilder.setConnectionManager(this.buildConnectionManager(sslContext, hostnameVerifier, this.timeoutProperties));
        }

        this.requestInterceptors.forEach(httpClientBuilder::addRequestInterceptorLast);

        httpClientBuilder.setDefaultRequestConfig(this.buildRequestConfig());
        CloseableHttpClient httpClient = httpClientBuilder.build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        return requestFactory;
    }

    private HttpClientConnectionManager buildConnectionManager(SSLContext sslContext, HostnameVerifier hostnameVerifier, TimeoutProperties timeoutProperties) {
        PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create();
        SSLConnectionSocketFactoryBuilder sslConnectionSocketFactoryBuilder = SSLConnectionSocketFactoryBuilder.create();
        if (sslContext != null) {
            sslConnectionSocketFactoryBuilder.setSslContext(sslContext);
        }

        if (hostnameVerifier != null) {
            sslConnectionSocketFactoryBuilder.setHostnameVerifier(hostnameVerifier);
        }

        connectionManagerBuilder.setSSLSocketFactory(sslConnectionSocketFactoryBuilder.build());
        if (timeoutProperties != null) {
            connectionManagerBuilder.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(Timeout.of((long)timeoutProperties.getSocketTimeout(), TimeUnit.MILLISECONDS)).build());
            connectionManagerBuilder.setDefaultConnectionConfig(ConnectionConfig.custom().setConnectTimeout(Timeout.of((long)timeoutProperties.getConnectTimeout(), TimeUnit.MILLISECONDS)).build());
        }

        return connectionManagerBuilder.build();
    }

    private RequestConfig buildRequestConfig() {
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        if (this.timeoutProperties != null) {
            requestConfigBuilder.setConnectionRequestTimeout(Timeout.of((long)this.timeoutProperties.getConnectRequestTimeout(), TimeUnit.MILLISECONDS));
        }

        this.requestConfigCustomizers.forEach((customizer) -> customizer.customize(requestConfigBuilder));
        return requestConfigBuilder.build();
    }
}
