package com.zenika.config;

import com.zenika.controller.ReactiveDrawingController;
import com.zenika.domain.Drawing;
import com.zenika.domain.DrawingInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.OPTIONS;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * Route configuration for the WEB reactive application.
 *
 * @author Guillaume DROUET
 */
//@Configuration
public class RouteConfig {

    /**
     * Defines the {@code RouterFunction} that will map the {@link ReactiveDrawingController} methods to REST endpoints.
     *
     * @param controller the mapped controller
     * @return the new bean
     */
    @Bean
    public RouterFunction<ServerResponse> routingFunction(final ReactiveDrawingController controller) {
        return getDrawingsRoute(controller).and(addDrawingRoute(controller)).and(addCorsRoute());
    }

    private static RouterFunction<ServerResponse> getDrawingsRoute(final ReactiveDrawingController controller) {
        return route(
                GET("/drawings"),
                req -> ok().contentType(MediaType.TEXT_EVENT_STREAM).body(controller.getDrawings(), DrawingInfo.class)
        );
    }

    private static RouterFunction<ServerResponse> addDrawingRoute(final ReactiveDrawingController controller) {
        return route(
                POST("/drawing").and(contentType(MediaType.APPLICATION_JSON)),
                req -> initJsonResponse().body(controller.add(req.bodyToMono(Drawing.class)), String.class)
        );
    }

    private static RouterFunction<ServerResponse> addCorsRoute() {
        return route(OPTIONS("/drawing*"), req -> ok().build());
    }

    private static ServerResponse.BodyBuilder initJsonResponse() {
        return ok().contentType(MediaType.APPLICATION_JSON);
    }
}
