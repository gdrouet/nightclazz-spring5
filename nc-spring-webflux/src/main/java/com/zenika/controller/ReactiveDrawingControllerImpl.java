package com.zenika.controller;

import com.zenika.domain.Drawing;
import com.zenika.domain.DrawingInfo;
import com.zenika.repository.DrawingRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A controller to read and create {@link Drawing drawings} entities.
 *
 * @author Guillaume DROUET
 */
public class ReactiveDrawingControllerImpl implements ReactiveDrawingController {

    private DrawingRepository drawingRepository;

    /**
     * Builds a new instance with its required repository.
     *
     * @param drawingRepository an abstract, standard drawing repository to perform CRUD operations
     */
    public ReactiveDrawingControllerImpl(final DrawingRepository drawingRepository) {
        this.drawingRepository = drawingRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<String> add(final Mono<Drawing> drawing) {
        return drawingRepository.saveAll(drawing).single().map(Drawing::getId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<DrawingInfo> getDrawings() {
        return drawingRepository.findAllBy();
    }
}
