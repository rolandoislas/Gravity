package com.rolandoislas.gravity.gui;

import com.rolandoislas.gravity.state.Game;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.gui.AbstractComponent;
import org.newdawn.slick.gui.TextField;
import org.newdawn.slick.state.StateBasedGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Rolando.
 */
public class ChatBox {

    private List<Label> messages = new ArrayList<>();
    private Rectangle box;
    private Rectangle tab;
    private List<String> queuedMessages = new ArrayList<>();
    private TextField textField;
    private Game game;
    private int totalLines;
    private int boxXMargin;
    private int boxYMargin;

    public ChatBox(Game game, GameContainer container) {
        this.game = game;
        createBox(container);
        createTab();
        createTextField(container);
        setTotalLines();
    }

    private void setTotalLines() {
        totalLines = (int) ((box.getHeight() - textField.getHeight()) / (new Label("Example").getHeight() + boxYMargin));
    }

    private void createTextField(GameContainer container) {
        int width = (int) box.getWidth();
        int height = (int) (box.getHeight() * 0.1);
        int x = (int) box.getX();
        int y = (int) (box.getY() + box.getHeight() - height);
        textField = new TextField(container, new Label("").getFont(), x, y, width, height);
        textField.setMaxLength(135);
        textField.addListener(e -> sendChatMessage());
    }

    private void sendChatMessage() {
        if((!textField.getText().equals("")) &&Keyboard.getEventKey() == Input.KEY_ENTER) {
            game.sendChatMessage(textField.getText());
            textField.setText("");
        }
    }

    private void createTab() {
        float width = box.getWidth() * 0.05f;
        float height = box.getHeight() * 0.25f;
        float x = box.getX() + box.getWidth();
        float y = box.getY();
        tab = new Rectangle(x, y, width, height);
    }

    private void createBox(GameContainer container) {
        float width = container.getWidth() * 0.25f;
        float height = container.getHeight() * 0.25f;
        float x = 0;
        float y = container.getHeight() - height;
        box = new Rectangle(x, y, width, height);
        boxXMargin = (int) (box.getWidth() * 0.02);
        boxYMargin = (int) (box.getHeight() * 0.01);
    }

    public void render(GameContainer container, Graphics g) {
        renderBox(g);
        renderTab(g);
        renderMessages(g);
        renderTextField(container, g);
    }

    private void renderTextField(GameContainer container, Graphics g) {
        textField.render(container, g);
    }

    private void renderMessages(Graphics g) {
        g.setColor(Color.gray);
        for(Label message : messages) {
            message.render(g);
        }
    }

    private void renderTab(Graphics g) {
        g.setColor(Color.black);
        g.fill(tab);
        g.setColor(Color.gray);
        g.draw(tab);
    }

    private void renderBox(Graphics g) {
        g.setColor(Color.black);
        g.fill(box);
        g.setColor(Color.gray);
        g.draw(box);
    }

    public void addMessageToQueue(String message) {
        queuedMessages.add(message);
    }

    public void addMessage() {
        if(queuedMessages.size() > 0) {
            for(String message : queuedMessages) {
                Label label = new Label(message);
                int messageNumber = messages.size();
                int x = (int) box.getX() + boxXMargin;
                int y = (int) (((messageNumber == 0) ? box.getY() : (messages.get(messageNumber - 1).getY() + messages.get(messageNumber - 1).getHeight())) + boxYMargin);
                label.setPosition(x, y);
                label.setBounds(box.getWidth() - boxXMargin * 2, box.getHeight() - boxYMargin);
                messages.add(label);
            }
            queuedMessages.clear();
            checkShiftMessages();
        }
    }

    private void checkShiftMessages() {
        int usedLines = 0;
        for(Label label : messages) {
            usedLines += label.getLines();
        }
        if(usedLines > totalLines) {
            int totalClipLines = usedLines - totalLines;
            int lines = 0;
            int clip = 0;
            for(Label label : messages) {
                clip++;
                lines += label.getLines();
                if(lines >= totalClipLines) {
                    break;
                }
            }
            shiftMessages(clip);
        }
    }

    private void shiftMessages(int clipSize) {
        for(int i = 0; i < clipSize; i++) {
            messages.remove(i);
        }
        List<Label> messagesCopy = messages.stream().collect(Collectors.toList());
        for(Label label : messagesCopy) {
            addMessageToQueue(label.getText());
        }
        messages.clear();
        addMessage();
    }

    public void mouseClicked(int button, int x, int y, int clickCount) {
        // tab click
        if(tab.contains(x, y)) {
            toggleChatBox();
        }
    }

    private void toggleChatBox() {
        if(box.getX() < 0) {
            box.setX(box.getX() + box.getWidth());
            moveMessagesX();
        } else {
            box.setX(box.getX() - box.getWidth());
            moveMessagesX();
        }
        createTab();
        moveMessagesX();
        moveTextFieldX();
    }

    private void moveTextFieldX() {
        textField.setLocation((int) box.getX(), textField.getY());
    }

    private void moveMessagesX() {
        int xMargin = (int) (box.getWidth() * 0.02);
        for(Label label : messages){
            label.setPosition((int)box.getX() + xMargin, (int)label.getY());
        }
    }

    public void update(GameContainer container, StateBasedGame game, int delta) {
        addMessage();
    }
}
