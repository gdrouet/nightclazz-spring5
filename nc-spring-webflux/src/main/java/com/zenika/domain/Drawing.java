package com.zenika.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * An entity that represents a drawing to be stored in a document data store.
 * 
 * @author Guillaume DROUET
 */
public class Drawing extends DrawingInfo {

    private String base64Image;

    public String getBase64Image() {
        return base64Image;
    }

    public void setBase64Image(final String base64Image) {
        this.base64Image = base64Image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Drawing drawing = (Drawing) o;

        if (getId() != null ? !getId().equals(drawing.getId()) : drawing.getId() != null) return false;
        if (getAuthor() != null ? !getAuthor().equals(drawing.getAuthor()) : drawing.getAuthor() != null) return false;
        return getBase64Image() != null ? getBase64Image().equals(drawing.getBase64Image()) : drawing.getBase64Image() == null;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getAuthor() != null ? getAuthor().hashCode() : 0);
        result = 31 * result + (getBase64Image() != null ? getBase64Image().hashCode() : 0);
        return result;
    }
}
