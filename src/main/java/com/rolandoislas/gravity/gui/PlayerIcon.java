package com.rolandoislas.gravity.gui;

import com.rolandoislas.gravity.entity.Ship;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

/**
 * @author Rolando Islas
 */
public class PlayerIcon {

    private final int id;
    private int height;
    private int width;
    private int x;
    private int y;
    private Ship ship;
    private Color shipColor;

    public PlayerIcon(int id, int width, int height) {
        this.id = id;
        this.width = width;
        this.height = height;
        shipColor = calculateColor(id);
        createShip();
    }

    public static Color calculateColor(int id) {
        switch (id) {
            case 1:
                return Color.green;
            case 2:
                return Color.blue;
            case 3:
                return Color.orange;
            case 4:
                return Color.red;
            default:
                return Color.white;
        }
    }

    private void createShip() {
        ship = new Ship(x, y, width, height);
        ship.setColor(shipColor);
    }

    public int getWidth() {
        return width;
    }

    public void render(Graphics g) {
        ship.render(g);
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        createShip();
    }

    public void setShipFilled(boolean filled) {
        ship.setFilled(filled);
    }
}
