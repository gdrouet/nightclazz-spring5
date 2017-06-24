package com.zenika;

import com.zenika.config.RouteConfig;
import com.zenika.controller.ReactiveDrawingController;
import com.zenika.domain.Drawing;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for reactive drawing application.
 *
 * @author Guillaume DROUET
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ReactiveDrawingControllerTest {

    /**
     * Configuration class for the tests.
     *
     * @author Guillaume DROUET
     */
    @Configuration
    @Import(RouteConfig.class)
    static class Config {

        /**
         * Builds a mocked {@link ReactiveDrawingController} that does not access to Mongo.
         * @return the bean
         */
        @Bean
        public ReactiveDrawingController mockController() {
            final ReactiveDrawingController controller = mock(ReactiveDrawingController.class);
            when(controller.add(any(Mono.class))).thenReturn(
                    Mono.just(someDrawing("3"))
            );

            when(controller.getDrawings()).thenReturn(
                    Flux.range(0, 3).map(String::valueOf).map(id -> someDrawing(id))
            );

            return controller;
        }
    }

    private WebTestClient client;

    private static Drawing someDrawing(final String id) {
        final Drawing drawing = new Drawing();
        drawing.setBase64Image("");
        drawing.setAuthor("gdrouet");
        drawing.setId(id);
        return drawing;
    }

    /**
     * Sets the router function and binds the {@link #client} to it.
     *
     * @param routerFunction the bean
     */
    @Autowired
    public void setReactiveDrawingController(final RouterFunction<ServerResponse> routerFunction) {
        client = WebTestClient.bindToRouterFunction(routerFunction).build();
    }

    @Test
    public void drawings() {
        client.get().uri("/drawings").accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Drawing.class)
                .hasSize(3)
                .contains(someDrawing("0"), someDrawing("1"), someDrawing("2"));
    }
}
