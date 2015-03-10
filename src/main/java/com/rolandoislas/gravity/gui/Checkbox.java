package com.rolandoislas.gravity.gui;

import com.rolandoislas.gravity.Main;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Rolando Islas
 */
public class Checkbox {

    private int x;
    private int y;
    private int size;
    private Rectangle box;
    private ActionListener clickListener;
    private boolean mouseDown;
    private boolean mouseUp;
    private Color boxBorder = new Color(255, 255, 255);
    private Color boxFill = new Color(255, 255, 255, 128);
    private boolean isChecked = false;

    public Checkbox(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
        createBox();
    }

    private void createBox() {
        box = new Rectangle(x, y, size, size);
    }


    public void render(Graphics g) {
        update();
        renderBox(g);
    }

    private void update() {
        checkClick();
    }

    private void checkClick() {
        if(Mouse.isButtonDown(0)) {
            mouseDown = true;
        }
        if(mouseDown && !Mouse.isButtonDown(0)) {
            mouseDown = false;
            mouseUp = true;
        }
        if(mouseUp && !Mouse.isButtonDown(0)) {
            mouseUp = false;
            clickPerformed();
        }
    }

    private void clickPerformed() {
        if(contains(Mouse.getX(), Main.getHeight() - 1 - Mouse.getY())) {
            doClickAction();
        }
    }

    private void doClickAction() {
        isChecked = !isChecked;
        if(!(clickListener == null)) {
            String command = "false";
            if(isChecked) {
                command = "true";
            }
            clickListener.actionPerformed(new ActionEvent(this, 0, command));
        }
    }

    private boolean contains(int x, int y) {
        return box.contains(x, y);
    }

    private void renderBox(Graphics g) {
        g.setColor(boxBorder);
        g.draw(box);
        if(isChecked) {
            g.setColor(boxFill);
            g.fill(box);
        }
    }

    public void addClickAction(ActionListener l) {
        clickListener = l;
    }

    public void reset() {
        isChecked = false;
    }
}
