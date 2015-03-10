package com.rolandoislas.gravity.gui;

import com.rolandoislas.gravity.Main;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.geom.Rectangle;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

/**
 * @author Rolando Islas
 */
public class Button {

    private final Font font;
    private Color disabledShapeColor;
    private Color shapeFocusedColor;
    private Color shapeBlurredColor;
    private Color textColor;
    private Color shapeColor;
    private Point position;
    private int height;
    private int width;
    private boolean enabled;
    private String title;
    private Rectangle buttonShape;
    private Color shapeOutlineColor;
    private Color disabledTextColor;
    private Point textPosition;
    private TrueTypeFont trueTypeFont;
    private ActionListener clickListener;
    private boolean mouseDown;
    private boolean mouseUp;
    private int textWidth;
    private int textHeight;

    public Button(String title, boolean enabled) {
        this.title = title;
        this.enabled = enabled;
        width = 100;
        height = 25;
        position = new Point(0, 0);
        shapeBlurredColor = new Color(0, 0, 0, 128);
        shapeColor = shapeBlurredColor;
        disabledShapeColor = shapeBlurredColor;
        shapeFocusedColor = new Color(0, 0, 0);
        textColor = new Color(255, 255, 255);
        shapeOutlineColor = new Color(0, 0, 0);
        disabledTextColor = new Color(255, 255, 255, 128);
        font = new Font("Helvetica", Font.BOLD, 16);
        trueTypeFont = new TrueTypeFont(font, true);
        createButtonShape();
        setTextPosition();
    }

    private void setTextPosition() {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        textWidth = (int)(font.getStringBounds(title, frc).getWidth());
        textHeight = (int)(font.getStringBounds(title, frc).getHeight());
        int x = (int) (position.getX() + ((width - textWidth) / 2));
        int y = (int) (position.getY() + ((height - textHeight) / 2));
        textPosition = new Point(x, y);
    }

    private void createButtonShape() {
        buttonShape = new Rectangle((float)position.getY(), (float)position.getX(), width, height);
    }

    public Button(String title) {
       this(title, true);
    }

    public Button() {
        this("", true);
    }

    public void render(Graphics g) {
        update();
        renderShape(g);
        renderText(g);
    }

    private void renderText(Graphics g) {
        g.setColor(enabled ? textColor : disabledTextColor);
        g.setFont(trueTypeFont);
        g.drawString(title, (float) textPosition.getX(), (float) textPosition.getY());
    }

    private void renderShape(Graphics g) {
        g.setColor(shapeOutlineColor);
        g.draw(buttonShape);
        g.setColor(shapeColor);
        g.fill(buttonShape);
    }

    public void setPosition(int x, int y) {
        position = new Point(x, y);
        buttonShape.setLocation(x, y);
        setTextPosition();
    }

    public int getPositionY() {
        return (int) position.getY();
    }

    public int getPositionX() {
        return (int) position.getX();
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
        buttonShape.setWidth(width);
        setTextPosition();
    }

    public void setHeight(int height) {
        this.height = height;
        buttonShape.setHeight(height);
        setTextPosition();
    }

    public boolean contains(int x, int y) {
        return buttonShape.contains(x, y);
    }

    public void hoverPerformed() {
        shapeColor = shapeFocusedColor;
    }

    public void hoverDone() {
        shapeColor = enabled ? shapeBlurredColor : disabledShapeColor;
    }

    public void update() {
        checkHover();
        if(enabled) {
            checkClick();
        }
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
        if(!(clickListener == null)) {
            clickListener.actionPerformed(new ActionEvent(new Object(), 0, ""));
        }
    }

    private void checkHover() {
        if(enabled && contains(Mouse.getX(), Main.getHeight() - 1 - Mouse.getY())) {
            hoverPerformed();
        } else {
            hoverDone();
        }
    }

    public void addClickAction(ActionListener l) {
        clickListener = l;
    }

    public Color getShapeColor() {
        return shapeColor;
    }

    public void setShapeColor(Color color, String type) {
        switch(type) {
            case "blur" :
                shapeBlurredColor = color;
                break;
            case "focus" :
                shapeFocusedColor = color;
                break;
            case "disabled" :
                disabledShapeColor = color;
                break;
            default:
                break;
        }
    }

    public void setTextColor(Color color) {
            textColor = color;
    }

    public void setText(String text) {
        this.title = text;
        setTextPosition();
        if(width < textWidth) {
            width = textWidth;
        }
        buttonShape.setWidth(width);
        setTextPosition();
    }

    public String getText() {
        return title;
    }

    public void disable() {
        shapeColor = disabledShapeColor;
        enabled = false;
    }

    public void enable() {
        shapeColor = shapeBlurredColor;
        enabled = true;
    }
}
