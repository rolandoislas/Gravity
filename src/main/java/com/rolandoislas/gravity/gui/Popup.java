package com.rolandoislas.gravity.gui;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rolando.
 */
public class Popup {

    private final float width;
    private final float height;
    private final float x;
    private final float y;
    private Rectangle box;
    private Label message;
    private boolean hidden;
    private List<Button> buttons = new ArrayList<>();

    public Popup(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        createComponents();
    }

    public Popup(GameContainer container) {
        width = container.getWidth() * .6f;
        height = container.getHeight() * .2f;
        x = container.getWidth() / 2 - width / 2;
        y = container.getHeight() / 2 - height / 2;
        createComponents();
    }

    private void createComponents() {
        createBox();
        createMessage();
    }

    private void createMessage() {
        message = new Label("");
        int xMargin = (int) (width * 0.01);
        int yMargin = (int) (height * 0.01);
        message.setPosition((int)x + xMargin, (int)y + yMargin);
        message.setBounds(width - xMargin * 2, height - yMargin);
    }

    private void createBox() {
        box = new Rectangle(x, y, width, height);
    }

    public void render(Graphics g) {
        if(!hidden) {
            renderBox(g);
            renderMessage(g);
            renderButtons(g);
        }
    }

    private void renderButtons(Graphics g) {
        for(Button button : buttons) {
            button.render(g);
        }
    }

    private void renderMessage(Graphics g) {
        message.render(g);
    }

    private void renderBox(Graphics g) {
        g.setColor(Color.black);
        g.fill(box);
        g.setColor(Color.white);
        g.draw(box);
    }

    public void setMessage(String messageString) {
        message.setText(messageString);
    }

    public void hide() {
        hidden = true;
    }

    public void show() {
        hidden = false;
    }

    public void addButton(Button button) {
        int buttonNumer = buttons.size() + 1;
        int padding = (int) (box.getWidth() * 0.01);
        int x = (int) ((box.getX() + box.getWidth()) - (button.getWidth() + padding) * buttonNumer);
        int y = (int) (box.getY() + box.getHeight() - button.getHeight() - padding);
        button.setPosition(x, y);
        buttons.add(button);
    }
}
