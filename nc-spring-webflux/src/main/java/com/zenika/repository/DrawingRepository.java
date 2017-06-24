package com.zenika.repository;

import com.zenika.domain.Drawing;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 * A repository to perform basic CRUD operations on {@link Drawing drawings} entities.
 *
 * @author Guillaume DROUET
 */
public interface DrawingRepository extends ReactiveCrudRepository<Drawing, String> {
}
