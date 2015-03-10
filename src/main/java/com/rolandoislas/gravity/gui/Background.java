package com.rolandoislas.gravity.gui;

import org.newdawn.slick.Image;

/**
 * @author Rolando Islas
 */
public class Background {

    private final Image image;
    private final int width;
    private final int height;

    public Background(int width, int height, Image image) {
        this.width = width;
        this.height = height;
        this.image = image;
    }

    public void render() {
        image.draw(0, 0, width, height);
    }

}
