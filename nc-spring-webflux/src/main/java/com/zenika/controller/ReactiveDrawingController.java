package com.zenika.controller;

import com.zenika.domain.Drawing;
import com.zenika.domain.DrawingInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveDrawingController {


    /**
     * Saves the drawing provided by the given {@code Mono}.
     *
     * @param drawing the drawing's publisher
     * @return a {@code Mono} that publishes the ID assigned to the saved drawing
     */
    Mono<String> add(Mono<Drawing> drawing);

    /**
     * Finds all drawings. This method relies on a capped collection and creates a tailable cursor to stream the data
     * from it.
     *
     * @return the drawings publisher
     */
    Flux<DrawingInfo> getDrawings();
}
