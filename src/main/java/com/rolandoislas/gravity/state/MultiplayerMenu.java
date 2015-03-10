package com.rolandoislas.gravity.state;

import com.rolandoislas.gravity.Main;
import com.rolandoislas.gravity.gui.Button;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rolando Islas
 */
public class MultiplayerMenu extends BasicGameState {

    private final Integer id;
    private Map<Integer, Button> buttons = new HashMap<>();
    private StateBasedGame game;
    private ActionListener initListener;

    public MultiplayerMenu(Integer id) {
        this.id = id;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        this.game = game;
        createButtons();
        positionButtons();
        setButtonSize();
    }

    private void setButtonSize() {
        for(int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setWidth(MainMenu.BUTTON_WIDTH);
        }
    }

    private void positionButtons() {
        buttons.get(0).setPosition(0, MainMenu.TOP_MARGIN);
        for(int i = 1; i < buttons.size(); i++) {
            buttons.get(i).setPosition(0, buttons.get(i - 1).getHeight() + buttons.get(i - 1).getPositionY() + 5);
        }
    }

    private void createButtons() {
        Button joinButton = new Button("Join Game");
        joinButton.addClickAction(e -> game.enterState(Main.STATE_ID.MULTIPLAYER_JOIN_MENU.id));
        buttons.put(0, joinButton);

        Button hostButton = new Button("Host Game");
        hostButton.addClickAction(e -> {
            MultiplayerLobby.setServer("localhost");
            MultiplayerLobby.setHost(true);
            game.enterState(Main.STATE_ID.MULTIPLAYER_LOBBY.id);
        });
        buttons.put(1, hostButton);

        Button backButton = new Button("Back");
        backButton.addClickAction(e -> game.enterState(Main.STATE_ID.MAIN_MENU.id));
        buttons.put(2, backButton);
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        renderBackground(g);
        renderButtons(g);
    }

    private void renderBackground(Graphics g) {
        MainMenu.background.render();
    }

    private void renderButtons(Graphics g) {
        for(int i = 0; i < buttons.size(); i++) {
            buttons.get(i).render(g);
        }
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
    }

}
