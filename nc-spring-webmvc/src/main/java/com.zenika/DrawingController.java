package com.zenika;

import com.zenika.domain.Drawing;
import com.zenika.domain.DrawingInfo;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Mono;

import javax.servlet.http.PushBuilder;
import java.awt.*;
import java.util.Base64;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A controller to serve WEB assets related to {@link Drawing} entities.
 *
 * @author Guillaume DROUET
 */
@Controller
public class DrawingController {

    private ReactiveMongoTemplate mongoTemplate;

    /**
     * Builds a new instance with its required template.
     *
     * @param mongoTemplate a that allows to read Mongo database
     */
    public DrawingController(final ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Gets the drawing associated to the specified ID.
     *
     * @param drawingId the drawing ID
     * @return the drawing's publisher
     */
    @ResponseBody
    @GetMapping(value = "/drawing/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getImage(@PathVariable("id") final String drawingId) {
        return mongoTemplate.findById(new ObjectId(drawingId), Drawing.class)
                .map(Drawing::getBase64Image)
                .map(Base64.getDecoder()::decode)
                .block();
    }

    @GetMapping("/")
    public String index(final PushBuilder pushBuilder) {
        mongoTemplate.find(findAllQuery(), DrawingInfo.class, "drawings")
                .collectList()
                .block()
                .forEach(performServerPush(pushBuilder));

        return "drawing.html";
    }

    private Query findAllQuery() {
        return new BasicQuery(new Document());
    }

    private Consumer<? super DrawingInfo> performServerPush(final PushBuilder pushBuilder) {
        return drawing -> pushBuilder.path("/drawing/" + drawing.getId()).push();
    }
}
