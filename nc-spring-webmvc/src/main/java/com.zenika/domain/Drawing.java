package com.zenika.domain;

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
}
