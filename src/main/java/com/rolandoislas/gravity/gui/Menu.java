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
public class Menu {

    private boolean hidden = false;
    private GameContainer container;
    private Rectangle box;
    private Label title;
    private List<Button> buttons = new ArrayList<>();

    public Menu(GameContainer container) {
        this.container = container;
        createBcx();
        createTitle();
    }

    private void createTitle() {
        title = new Label("");
    }

    private void createBcx() {
        float width = container.getWidth() * 0.1f;
        float height = container.getHeight() * 0.2f;
        float x = container.getWidth() / 2 - width / 2;
        float y = container.getHeight() / 2 - height / 2;
        box = new Rectangle(x, y, width, height);
    }

    public void setTitle(String title) {
        this.title.setText(title);
        int x = (int) (box.getX() + (box.getWidth() - this.title.getWidth()) / 2);
        int y = (int) (box.getY() + box.getHeight() * 0.01);
        this.title.setPosition(x, y);
    }

    public void addButton(Button button) {
        int buttonNumber = buttons.size();
        int x = (int) (box.getX() + (box.getWidth() - button.getWidth()) / 2);
        float margin = box.getHeight() * 0.01f;
        int y = (int) (title.getY() + title.getHeight() + margin * buttonNumber + button.getHeight() * (buttonNumber));
        button.setPosition(x, y);
        buttons.add(button);
    }

    public void toggle() {
        hidden = !hidden;
    }

    public void hide() {
        hidden = true;
    }

    public void render(Graphics g) {
        if(!hidden) {
            renderBox(g);
            renderTitle(g);
            renderButtons(g);
        }
    }

    private void renderButtons(Graphics g) {
        for(Button button : buttons) {
            button.render(g);
        }
    }

    private void renderTitle(Graphics g) {
        title.render(g);
    }

    private void renderBox(Graphics g) {
        g.setColor(Color.black);
        g.fill(box);
        g.setColor(Color.white);
        g.draw(box);
    }
}
