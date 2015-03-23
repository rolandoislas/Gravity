package com.rolandoislas.gravity.state;

import com.rolandoislas.gravity.Main;
import com.rolandoislas.gravity.gui.Background;
import com.rolandoislas.gravity.gui.Button;
import com.rolandoislas.gravity.gui.Popup;
import org.newdawn.slick.*;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rolando Islas
 */
public class MainMenu extends BasicGameState {

    public static final int BUTTON_WIDTH = 150;
    public static final int TOP_MARGIN = 50;
    private static Integer id;
    private static Map<Integer, Button> buttons = new HashMap<Integer, Button>();
    private static  StateBasedGame game;
    public static Background background;
    private static Image backgroundImage;
    private ActionListener initListener;
	private boolean error;
	private Popup errorPopup;

	public MainMenu(Integer id) {
        MainMenu.id = id;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        MainMenu.game = game;
        createButtons(game);
        positionButtons();
        setButtonsSize();
        createBackground();
		createErrorPopup(container);
    }

	private void createErrorPopup(GameContainer container) {
		errorPopup = new Popup(container);
		errorPopup.hide();
		Button button = new Button();
		button.setText("Ok");
		button.addClickAction(e -> errorPopup.hide());
		errorPopup.addButton(button);
	}

	private void createBackground() {
        try {
            backgroundImage = new Image("images/background/black-hole.jpg");
        } catch (SlickException e) {
            e.printStackTrace();
        }
        background = new Background(Main.getWidth(), Main.getHeight(), backgroundImage);
    }

    private void setButtonsSize() {
        for(int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setWidth(BUTTON_WIDTH);
        }
    }

    private void positionButtons() {
        buttons.get(0).setPosition(0, TOP_MARGIN);
        for (int i = 1; i < buttons.size(); i++) {
            buttons.get(i).setPosition(0, buttons.get(i - 1).getHeight() + buttons.get(i - 1).getPositionY() + 5);
        }
    }

    private void createButtons(StateBasedGame game) {
        Button singlePlayerButton = new Button("Singleplayer", false);
        buttons.put(0, singlePlayerButton);

        Button multiPlayerButton = new Button("Multiplayer");
        multiPlayerButton.addClickAction(e -> game.enterState(Main.STATE_ID.MULTIPLAYER_MENU.id));
        buttons.put(1, multiPlayerButton);

        Button optionsButton = new Button("Options", false);
        buttons.put(2, optionsButton);

        Button exitButton = new Button("Exit Game");
        exitButton.addClickAction(e -> game.getContainer().exit());
        buttons.put(3, exitButton);
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        renderBackground(g);
        renderButtons(g);
		errorPopup.render(g);
	}

    private void renderBackground(Graphics g) {
        background.render();
    }

    private void renderButtons(Graphics g) {
        for(int i = 0; i < buttons.size(); i++) {
            buttons.get(i).render(g);
        }
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
    }

	@Override
	public void enter(GameContainer container, StateBasedGame game) throws SlickException{
		if (error)
			errorPopup.show();
	}

	public void setError(String errorMessage) {
		this.error = true;
		errorPopup.setMessage(errorMessage);
	}
}
