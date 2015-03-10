package com.rolandoislas.gravity.entity;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Transform;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Rolando Islas
 */
public class Ship {

    private int x;
    private int y;
    private int width;
    private int height;
    private Color color = Color.white;
    private Polygon shipPolygon;
    private boolean filled = false;

    public Ship(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        createPolygon();
    }

    private void createPolygon() {
        Point[] basePoints = {
                new Point(7.5f, 16),
                new Point(9, 13),
                new Point(9, 11),
                new Point(15, 5),
                new Point(15, 4),
                new Point(9, 4),
                new Point(9, 2),
                new Point(11, 0),
                new Point(4, 0),
                new Point(6, 2),
                new Point(6, 4),
                new Point(0, 4),
                new Point(0, 5),
                new Point(6, 11),
                new Point(6, 13)
        };
        shipPolygon = new Polygon();
        BigDecimal scaleX = new BigDecimal(16).divide(new BigDecimal(width), 9, BigDecimal.ROUND_CEILING);
        BigDecimal scaleY = new BigDecimal(15).divide(new BigDecimal(height), 9, BigDecimal.ROUND_CEILING);
        for (Point basePoint : basePoints) {
            float x = new BigDecimal(basePoint.getX()).divide(scaleX, 9, BigDecimal.ROUND_CEILING).floatValue();
            float y = new BigDecimal(basePoint.getY()).divide(scaleY, 9, BigDecimal.ROUND_CEILING).floatValue();
            shipPolygon.addPoint(x, y);
        }
        //shipPolygon.setLocation(x, y);
        shipPolygon.setX(x);
        shipPolygon.setY(y);
        shipPolygon = (Polygon) shipPolygon.transform(Transform.createRotateTransform(3.14159265f, shipPolygon.getCenterX(), shipPolygon.getCenterY()));
        shipPolygon = (Polygon) shipPolygon.transform(Transform.createTranslateTransform(0, 15));
    }

    public void render(Graphics g) {
        g.setColor(color);
        g.draw(shipPolygon);
        if(filled) {
            g.fill(shipPolygon);
        }
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        createPolygon();
    }

    public void moveCenter(int x, int y) {
        this.x = x;
        this.y = y;
        shipPolygon.setCenterX(x);
        shipPolygon.setCenterY(y);
    }

    public void setFilled(boolean filled) {
        this.filled = filled;
    }
}
