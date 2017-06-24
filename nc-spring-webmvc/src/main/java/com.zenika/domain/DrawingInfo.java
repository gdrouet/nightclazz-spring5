package com.zenika.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * An entity that represents drawing information to be stored in a document data store.
 * 
 * @author Guillaume DROUET
 */
@Document(collection = "drawings")
public class DrawingInfo {

    @Id
    private String id;

    private String author;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return "DrawingInfo{" +
                "id='" + id + '\'' +
                ", author='" + author + '\'' +
                '}';
    }
}
