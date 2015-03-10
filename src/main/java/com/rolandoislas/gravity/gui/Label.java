package com.rolandoislas.gravity.gui;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.TrueTypeFont;

import java.awt.Font;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rolando Islas
 */
public class Label {

    private String text;
    private List<String> textList = new ArrayList<>();
    private Point position = new Point(0, 0);
    private Font awtFont = new Font("Helvetica", Font.PLAIN, 12);
    private TrueTypeFont font = new TrueTypeFont(awtFont, true);
    private int width;
    private int height;
    private float xBound;
    private float yBound;
    private boolean bounds = false;

    public Label(String text) {
        this.text = text;
        createTextList();
        setSize();
    }

    public Label(Label label) {
        this.text = label.text;
        this.textList = label.textList;
        this.position = label.position;
        this.awtFont = label.awtFont;
        this.font = label.font;
        this.width = label.width;
        this.height = label.height;
        this.xBound = label.xBound;
        this.yBound = label.yBound;
        this.bounds = label.bounds;
    }

    private void createTextList() {
        textList.clear();
        if(bounds) {
            String[] words = text.split(" ");
            int currentLineWidth = 0;
            String line = "";
            for(int i = 0; i < words.length; i++) {
                int wordWidth = font.getWidth((i == 0) ? words[i] : " " + words[i]);
                if(wordWidth + currentLineWidth > xBound) {
                    textList.add(line);
                    line = words[i];
                    currentLineWidth = wordWidth;
                } else {
                    line += (i == 0) ? words[i] : " " + words[i];
                    currentLineWidth += wordWidth;
                }
                if(i == words.length - 1) {
                    textList.add(line);
                }
            }

        } else {
            textList.add(text);
        }
    }

    private void setSize() {
        height = 0;
        int largestWidth = 0;
        for(String string : textList) {
            if(font.getWidth(string) > largestWidth) {
                largestWidth = font.getWidth(string);
            }
            height += font.getHeight(string);
        }
        width = largestWidth;
    }

    public void setFont(String name, int style, int size) {
        awtFont = new Font(name, style, size);
        font = new TrueTypeFont(awtFont, true);
        setSize();
    }

    public void setPosition(int x, int y) {
        position = new Point(x, y);
    }

    public void render(Graphics g) {
        g.setFont(font);
        g.setColor(Color.white);
        // Render String
        int index = 0;
        for(String textPart : textList) {
            g.drawString(textPart, (float) (position.getX()), (float) (position.getY() + font.getHeight("Example") * index));
            index++;
        }

    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setText(String text) {
        this.text = text;
        createTextList();
        setSize();
    }

    public double getX() {
        return position.getX();
    }

    public double getY() {
        return position.getY();
    }

    public void setBounds(float xBound, float yBound) {
        bounds = true;
        this.xBound = xBound;
        this.yBound = yBound;
        createTextList();
        setSize();
    }

    public String getText() {
        return text;
    }

    public TrueTypeFont getFont() {
        return font;
    }

    public int getLines() {
        return textList.size();
    }
}
