package com.zenika;

import com.mongodb.reactivestreams.client.MongoClient;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for the WEB MVC application.
 *
 * @author Guillaume DROUET
 */
@Configuration
public class WebmvcConfig implements WebMvcConfigurer {

    /**
     * {@inheritDoc}
     */
    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("drawing");
    }

    /**
     * Builds a new customizer that will enable HTTP/2.
     *
     * @param serverProperties the properties providing SSL configuration
     * @return the bean
     */
    @Bean
    public JettyHttp2Customizer customizer(final ServerProperties serverProperties) {
        return new JettyHttp2Customizer(serverProperties);
    }

    /**
     * Builds a template that will be used to query the Mongo database.
     * @param mongoClient the mongo client
     * @return the bean
     */
    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate(final MongoClient mongoClient) {
        return new ReactiveMongoTemplate(mongoClient, "nightclazz");
    }
}
