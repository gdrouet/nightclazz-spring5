package com.zenika.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Configures CORS.
 *
 * @author Guillaule DROUET
 */
@Configuration
@EnableWebFlux
public class CorsConfig implements WebFluxConfigurer {

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        addCorsMapping("/drawings", "GET", registry);
        addCorsMapping("/drawing", "POST", registry);
    }

    private void addCorsMapping(final String pathPattern, final String method, final CorsRegistry corsRegistry) {
        corsRegistry.addMapping(pathPattern)
                .allowedMethods(method)
                .allowedOrigins("https://localhost:8443")
                .allowedHeaders("Content-Type");
    }
}
