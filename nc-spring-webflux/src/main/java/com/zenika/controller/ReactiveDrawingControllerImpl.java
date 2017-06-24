package com.zenika.controller;

import com.zenika.domain.Drawing;
import com.zenika.domain.DrawingInfo;
import com.zenika.repository.DrawingRepository;
import org.bson.Document;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A controller to read and create {@link Drawing drawings} entities.
 *
 * @author Guillaume DROUET
 */
public class ReactiveDrawingControllerImpl implements ReactiveDrawingController {

    private ReactiveMongoTemplate mongoTemplate;

    private DrawingRepository drawingRepository;

    /**
     * Builds a new instance with its required repositories.
     *
     * @param mongoTemplate a low level repository that allows to create tailable cursor
     * @param drawingRepository an abstract, standard drawing repository to perform CRUD operations
     */
    public ReactiveDrawingControllerImpl(final ReactiveMongoTemplate mongoTemplate,
                                         final DrawingRepository drawingRepository) {
        this.mongoTemplate = mongoTemplate;
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
        return mongoTemplate.tail(findAllQuery(), DrawingInfo.class);
    }

    private Query findAllQuery() {
        // represents find({})
        return new BasicQuery(new Document());
    }
}
