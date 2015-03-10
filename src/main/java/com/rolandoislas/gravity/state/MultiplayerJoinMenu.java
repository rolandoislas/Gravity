package com.rolandoislas.gravity.state;

import com.rolandoislas.gravity.Main;
import com.rolandoislas.gravity.gui.Button;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.gui.TextField;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Rolando Islas
 */
public class MultiplayerJoinMenu extends BasicGameState {

    private int id;
    private TextField textField;
    private Button backButton;
    private Button connectButton;
    private ActionListener initListener;

    public MultiplayerJoinMenu(int id){
        this.id = id;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        createTextField(container);
        createBackButton(game);
        createConnectButton(container, game);
    }

    private void createConnectButton(GameContainer container, StateBasedGame game) {
        int x = backButton.getPositionX() - backButton.getWidth() - 5;
        int y = backButton.getPositionY();
        connectButton = new Button("Connect");
        connectButton.setPosition(x, y);
        connectButton.addClickAction(e -> connectToLobby(game));
    }

    private void connectToLobby(StateBasedGame game) {
        MultiplayerLobby.setServer(textField.getText());
        game.enterState(Main.STATE_ID.MULTIPLAYER_LOBBY.id);
    }

    private void createBackButton(StateBasedGame game) {
        int width = 100;
        int x = textField.getX() + textField.getWidth() - width;
        int y = textField.getY() + textField.getHeight() + 5;
        backButton = new Button("Back");
        backButton.setPosition(x, y);
        backButton.addClickAction(e -> game.enterState(Main.STATE_ID.MAIN_MENU.id));
    }

    private void createTextField(GameContainer container) {
        int width = 500;
        int height = 50;
        int x = container.getWidth() / 2 - width / 2;
        int y = container.getHeight() / 2 - height / 2;
        textField = new TextField(container, new TrueTypeFont(new Font("Helvetica", Font.BOLD, 36), true), x, y, width, height);
        textField.setBorderColor(new Color(0, 0, 0, 255));
        textField.setBackgroundColor(new Color(0, 0, 0, 128));
        textField.setText("localhost");
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        MainMenu.background.render();
        renderTextField(container, g);
        renderBackButton(g);
        renderConnectButton(g);
    }

    private void renderConnectButton(Graphics g) {
        connectButton.render(g);
    }

    private void renderBackButton(Graphics g) {
        backButton.render(g);
    }

    private void renderTextField(GameContainer container, Graphics g) {
        textField.render(container, g);
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
    }

}
