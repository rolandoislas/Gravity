package com.rolandoislas.gravity.gui;

import com.rolandoislas.gravity.logic.MovementPiece;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rolando.
 */
public class MovementSelectPanel {

    private List<MovementPiece> movementPieces = new ArrayList<>();
    private GameContainer container;
    private Rectangle box;
    private Color boxColor = new Color(0, 0, 0, 255);
    private Color boxBorderColor = new Color(128, 128, 128, 255);
    private Label title;
    private Rectangle titleBox;
    private Color titleBoxColor = new Color(128, 128, 128, 255);
    private List<Button> pieceButtons = new ArrayList<>();
    private ActionListener clickListener;
    private int buttonXMargin;
    private float boxWidth;
    private float boxHeight;
    private int buttonSize;

    public MovementSelectPanel(GameContainer container) {
        this.container = container;
        createPieces();
        createBox();
        createTitle();
        createTitleBox();
        positionBoxPieces();
    }

    private void positionBoxPieces() {
        int index = 0;
        for(Button button : pieceButtons) {
            int x = (int) (box.getX() + buttonXMargin * (index + 1) + buttonSize * index);
            int y = (int) (box.getY() + box.getHeight() * 0.025);
            button.setPosition(x, y);
            index++;
        }
    }

    private void createPieces() {
        buttonSize = (int) (container.getHeight() * 0.095);
        buttonXMargin = (int) (container.getWidth() * 0.001605);
        for(int i = 0; i < 6; i++) {
            Button button = new Button();
            button.setWidth(buttonSize);
            button.setHeight(buttonSize);
            button.setShapeColor(new Color(128, 128, 128), "blur");
            button.setShapeColor(new Color(255, 255, 255), "focus");
            button.setShapeColor(new Color(0, 0, 0), "disabled");
            button.setTextColor(new Color(0, 0, 0));
            button.addClickAction(e -> pieceClicked(button));
            pieceButtons.add(button);
        }
        boxWidth = (float) (pieceButtons.get(0).getWidth() * pieceButtons.size() + buttonXMargin * (pieceButtons.size() + 1) - buttonXMargin * 0.5);
        boxHeight = (float) (pieceButtons.get(0).getHeight() + container.getHeight() * 0.004);
    }

    private void pieceClicked(Button button) {
        String code = button.getText();
        clickListener.actionPerformed(new ActionEvent(button, 0, code));
    }

    private void updatePieces() {
        int index = 0;
        for(Button button : pieceButtons) {
            button.setText(movementPieces.get(index).getCode());
            button.enable();
            index++;
        }
    }

    private void createTitleBox() {
        float x = box.getX();
        double titleMargin = (box.getY() - (title.getY() + title.getHeight())) * 2;
        float y = (float) (box.getY() - titleMargin - title.getHeight());
        float width = box.getWidth();
        float height = (float) (titleMargin + title.getHeight());
        titleBox = new Rectangle(x, y, width, height);
    }

    private void createTitle() {
        title = new Label("Select your movement piece.");
        title.setFont("Helvetica", Font.PLAIN, 22);
        int x = (int) ((box.getWidth() - title.getWidth()) / 2 + box.getX());
        int y = (int) (box.getY() - title.getHeight() - container.getHeight() * 0.01);
        title.setPosition(x, y);
    }

    private void createBox() {
        float boxX = container.getWidth() / 2 - boxWidth / 2;
        float boxY = container.getHeight() / 2 - boxHeight / 2;
        box = new Rectangle(boxX, boxY, boxWidth, boxHeight);
    }

    public void setPieces(List<MovementPiece> movementPieces) {
        this.movementPieces = movementPieces;
        updatePieces();
    }

    public void render(Graphics g) {
        renderBox(g);
        renderTitleBox(g);
        renderTitle(g);
        renderPieces(g);
    }

    private void renderPieces(Graphics g) {
        for(Button pieceButton : pieceButtons) {
            pieceButton.render(g);
        }
    }

    private void renderTitleBox(Graphics g) {
        g.setColor(titleBoxColor);
        g.fill(titleBox);
        g.draw(titleBox);
    }

    private void renderTitle(Graphics g) {
        title.render(g);
    }

    private void renderBox(Graphics g) {
        g.setColor(boxColor);
        g.fill(box);
        g.setColor(boxBorderColor);
        g.draw(box);

    }

    public void addClickAction(ActionListener l) {
        clickListener = l;
    }

    private void move(double newX, double newY, double oldX, double oldY) {
        float x;
        float y;
        float xDif = (float) (oldX - newX);
        float yDif = (float) (oldY - newY);
        // Main box
        x = box.getX() - xDif;
        y = box.getY() - yDif;
        box.setLocation(x, y);
        // Title Box
        x = titleBox.getX() - xDif;
        y = titleBox.getY() - yDif;
        titleBox.setLocation(x, y);
        // Title
        x = (float)title.getX() - xDif;
        y = (float)title.getY() - yDif;
        title.setPosition((int)x, (int)y);
        // Pieces
        for(Button button : pieceButtons) {
            x = (float)button.getPositionX() - xDif;
            y = (float)button.getPositionY() - yDif;
            button.setPosition((int)x, (int)y);
        }
    }

    public void mouseDragged(int oldX, int oldY, int newX, int newY) {
        if(titleBox.contains(oldX, oldY)) {
            move(newX, newY, oldX, oldY);
        }
    }
}
