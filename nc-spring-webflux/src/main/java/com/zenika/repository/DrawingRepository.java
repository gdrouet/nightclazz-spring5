package com.zenika.repository;

import com.zenika.domain.Drawing;
import com.zenika.domain.DrawingInfo;
import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

/**
 * A repository to perform basic CRUD operations on {@link Drawing drawings} entities.
 *
 * @author Guillaume DROUET
 */
public interface DrawingRepository extends ReactiveCrudRepository<Drawing, String> {

    /**
     * <p>
     * Finds all drawings. The produced {@code Flux} is connected to a tailable cursor.
     * </p>
     *
     * @return the publisher
     */
    @Tailable
    Flux<DrawingInfo> findAllBy();
}
