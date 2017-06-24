package com.zenika.config;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for custom {@code ReactiveWebServerFactory}.
 *
 * @author Guillaume DROUET
 */
@Configuration
public class ReactiveWebServerFactoryConfig {

    /**
     * Builds a new {@link ReactiveWebServerFactory} that will configure SSL.
     *
     * @param serverProperties the server properties to adjust configuration
     * @return the new bean
     */
    @Bean
    public ReactiveWebServerFactory reactiveWebServerFactory(final ServerProperties serverProperties) {
        return new CustomJettyReactiveWebServerFactory(serverProperties);
    }
}
