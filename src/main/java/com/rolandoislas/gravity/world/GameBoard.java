package com.rolandoislas.gravity.world;

import com.rolandoislas.gravity.Main;
import com.rolandoislas.gravity.entity.Ship;
import com.rolandoislas.gravity.gui.Background;
import com.rolandoislas.gravity.gui.PlayerIcon;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Path;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Shape;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rolando Islas
 */
public class GameBoard {

    private static final int totalLocations = 30;
    private final int totalPlayers;
    private final int width;
    private final int height;
    private final Point center;
    private Background background;
    private Circle blackhole;
    private Path spiral;
    private List<Shape> boardLocations;
    private List<Ship> players = new ArrayList<>();
    private List<Ship> neutralShips = new ArrayList<>();
    private int renderTop = 1;

    public GameBoard(int players, int width, int height) {
        this.totalPlayers = players;
        this.width = width;
        this.height = height;
        center = new Point(width / 2, height / 2);
        createBackground();
        createBoard();
        createPlayers();
        createNeutralShips();
    }

    private void createNeutralShips() {
        for(int i = 0; i < 2; i++) {
            Ship neutralShip = new Ship(0, 0, (int)boardLocations.get(0).getWidth(), (int)boardLocations.get(0).getWidth());
            neutralShip.moveCenter((int) center.getX(), (int) center.getY());
            neutralShip.setColor(new Color(128, 128, 128));
            neutralShip.setFilled(true);
            neutralShips.add(neutralShip);
        }
    }

    private void createPlayers() {
        for(int i = 0; i < totalPlayers; i++) {
            Ship player = new Ship(0, 0, (int)boardLocations.get(0).getWidth(), (int)boardLocations.get(0).getWidth());
            player.moveCenter((int) center.getX(), (int) center.getY());
            player.setColor(PlayerIcon.calculateColor(i + 1));
            player.setFilled(true);
            players.add(player);
        }
    }

    private void createBoard() {
        // Create block hole
        blackhole = new Circle(center.getX(), center.getY(), 50);

        // Create spiral path
        spiral = new Path(center.getCenterX(), center.getCenterY());
        double a = 15;
        double b = 15;
        for(int i = 0; i < 720; i++) {
            double angle = 0.1 * i;
            double x = center.getX() + (a + b * angle) * Math.cos(angle);
            double y = center.getY() + (a + b * angle) * Math.sin(angle);
            spiral.lineTo((float)x, (float)y);
        }

        // Create board locations
        boardLocations = new ArrayList<>();
        int step = 20;
        for(int i = 0; i < totalLocations; i++) {
            float[] point = spiral.getPoint((int) (step * i / 2.5));
            float size = 50 - (40 - i);
            Circle circle = new Circle(0, 0, size);
            circle.setCenterX(point[0]);
            circle.setCenterY(point[1]);
            boardLocations.add(circle);
        }
    }

    private void createBackground() {
        Image backgroundImage = null;
        try {
            backgroundImage = new Image("images/background/star-field.jpg");
        } catch (SlickException e) {
            e.printStackTrace();
        }
        background = new Background(Main.getWidth(), Main.getHeight(), backgroundImage);
    }

    public void render(Graphics g) {
        renderBackground();
        renderBoard(g);
        renderNeutralShips(g);
        renderPlayers(g);
    }

    private void renderNeutralShips(Graphics g) {
        for(Ship ship : neutralShips) {
            ship.render(g);
        }
    }

    private void renderPlayers(Graphics g) {
        for(Ship player : players) {
            player.render(g);
        }
        players.get(renderTop - 1).render(g);
    }

    private void renderBoard(Graphics g) {
        // Blackhole
        g.setColor(Color.black);
        g.fill(blackhole);

        // Spiral
        g.setColor(Color.gray);
        g.draw(spiral);

        // Board Locations
        //g.setColor(Color.gray);
        boardLocations.forEach(g::draw);
    }

    private void renderBackground() {
        background.render();
    }

    public void movePlayer(int playerNumber, int slot) {
        Ship player = players.get(playerNumber - 1);
        player.setSize((int)boardLocations.get(slot).getWidth(), (int)boardLocations.get(slot).getHeight());
        player.moveCenter((int) boardLocations.get(slot).getCenterX(), (int) boardLocations.get(slot).getCenterY());
    }

    public void setRenderTop(int playerNumber) {
        this.renderTop = playerNumber;
    }

    public static int getTotalLocations() {
        return totalLocations;
    }

    public void moveNeutralShip(int shipNumber, int location) {
        Ship ship = neutralShips.get(shipNumber - 1);
        ship.setSize((int)boardLocations.get(location).getWidth(), (int)boardLocations.get(location).getHeight());
        ship.moveCenter((int) boardLocations.get(location).getCenterX(), (int) boardLocations.get(location).getCenterY());
    }

    public void reset() {

    }
}
