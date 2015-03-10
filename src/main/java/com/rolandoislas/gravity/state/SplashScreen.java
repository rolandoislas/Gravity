package com.rolandoislas.gravity.state;

import com.rolandoislas.gravity.Main;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.GameState;
import org.newdawn.slick.state.StateBasedGame;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rolando.
 */
public class SplashScreen extends BasicGameState {

    private int id;
    private List<GameState> states = new ArrayList<>();
    private boolean updated;

    public SplashScreen(int id) {
        this.id = id;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {

    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) throws SlickException {

    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        g.setBackground(Color.darkGray);
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        if(updated) {
            addStates(container, game);
        }
        updated = true;
    }

    private void addStates(GameContainer container, StateBasedGame game) {
        states.add(new Game(Main.STATE_ID.GAME.id));
        states.add(new MainMenu(Main.STATE_ID.MAIN_MENU.id));
        states.add(new MultiplayerMenu(Main.STATE_ID.MULTIPLAYER_MENU.id));
        states.add(new MultiplayerLobby(Main.STATE_ID.MULTIPLAYER_LOBBY.id));
        states.add(new MultiplayerJoinMenu(Main.STATE_ID.MULTIPLAYER_JOIN_MENU.id));
        for(GameState state : states) {
            try {
                state.init(container, game);
            } catch (SlickException e) {
                e.printStackTrace();
            }
            game.addState(state);
        }
        game.enterState(Main.STATE_ID.MAIN_MENU.id);
    }
}
