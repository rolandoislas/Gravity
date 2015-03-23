package com.rolandoislas.gravity.state;

import com.rolandoislas.gravity.Main;
import com.rolandoislas.gravity.gui.Button;
import com.rolandoislas.gravity.gui.ChatBox;
import com.rolandoislas.gravity.gui.Menu;
import com.rolandoislas.gravity.gui.MovementSelectPanel;
import com.rolandoislas.gravity.gui.PlayerPanelGame;
import com.rolandoislas.gravity.gui.Popup;
import com.rolandoislas.gravity.logic.MovementPiece;
import com.rolandoislas.gravity.net.client.game.GameClientInitializer;
import com.rolandoislas.gravity.net.server.game.GameServerInitializer;
import com.rolandoislas.gravity.world.GameBoard;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rolando Islas
 */
public class Game extends BasicGameState {

    private final int id;
    private GameBoard gameboard;
    private String serverIP = "localhost";
    private boolean host = false;
    private int players;
    private boolean netFailed;
    private GameServerInitializer gameServer;
    private GameClientInitializer gameClient;
    private List<PlayerPanelGame> playerList = new ArrayList<>();
    private int playerNumber;
    private boolean turn;
    private MovementSelectPanel movementSelectPanel;
    private Popup popup;
    private Menu menu;
    private StateBasedGame game;
    private ChatBox chatBox;

    public Game(int id) {
        this.id = id;
    }

    public void setServer(String server) {
        this.serverIP = server;
    }

    public void setHost(boolean host) {
        this.host = host;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        this.game = game;
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        renderGameboard(g);
        renderPlayerList(g);
        renderMovementSelectPanel(g);
        renderPopup(g);
        renderEscapeMenu(g);
        renderChatBox(container, g);
    }

    private void renderChatBox(GameContainer container, Graphics g) {
        chatBox.render(container, g);
    }

    private void renderEscapeMenu(Graphics g) {
        menu.render(g);
    }

    private void renderPopup(Graphics g) {
        popup.render(g);
    }

    private void renderMovementSelectPanel(Graphics g) {
        if(turn) {
            movementSelectPanel.render(g);
        }
    }

    private void renderPlayerList(Graphics g) {
        for (PlayerPanelGame panel : playerList) {
            panel.render(g);
        }
    }

    private void renderGameboard(Graphics g) {
        gameboard.render(g);
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        chatBox.update(container, game, delta);
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) throws SlickException {
        createComponents(container);
        if(host) {
            startServer(game);
        }
        startClient(game);
    }

    private void startClient(StateBasedGame game) {
        Runnable client = () -> {
            try {
                gameClient = new GameClientInitializer(this, serverIP, GameServerInitializer.port);
                gameClient.run();
            } catch (Exception e) {
                e.printStackTrace();
                doFail(game, "Failed to join server.");
            }
        };
        new Thread(client).start();
        connectToServer();
    }

    private void connectToServer() {
        do {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while(!netFailed && gameClient.channel() == null);
        if(!netFailed) {
            gameClient.connect();
            gameClient.requestGameState();
        }
    }

    private void startServer(StateBasedGame game) {
        Runnable server = () -> {
            try {
                gameServer = new GameServerInitializer();
                gameServer.run();
            } catch (Exception e) {
                e.printStackTrace();
                doFail(game, "Failed to create server.");
            }
        };
        new Thread(server).start();
    }

    private void doFail(StateBasedGame game, String message) {
        // TODO Display message with confirmation.
        netFailed = true;
        game.enterState(Main.STATE_ID.MAIN_MENU.id);
    }

    private void createComponents(GameContainer container) {
        createGameboard(container);
        createPlayerList(container);
        createMovementSelectPanel(container);
        createPopup(container);
        createEscapeMenu(container);
        createChatBox(container);
    }

    private void createChatBox(GameContainer container) {
        chatBox = new ChatBox(this, container);
    }

    private void createEscapeMenu(GameContainer container) {
        menu = new Menu(container);
        menu.hide();
        menu.setTitle("Menu");
        Button button = new Button("Exit Game");
        button.addClickAction(e -> changeToMenuState());
        menu.addButton(button);
    }

    private void createPopup(GameContainer container) {
        popup = new Popup(container);
        popup.hide();
        Button button = new Button();
        button.setText("Exit to Menu");
        button.addClickAction(e -> changeToMenuState());
        popup.addButton(button);
        button = new Button();
        button.setText("View Gameboard");
        button.addClickAction(e -> hidePopup());
        popup.addButton(button);
    }

    private void hidePopup() {
        popup.hide();
    }

    private void changeToMenuState() {
        game.enterState(Main.STATE_ID.MAIN_MENU.id);
    }

    private void createMovementSelectPanel(GameContainer container) {
        movementSelectPanel = new MovementSelectPanel(container);
        movementSelectPanel.addClickAction(this::movementPieceClicked);
    }

    private void movementPieceClicked(ActionEvent e) {
        Button button = (Button)e.getSource();
        button.disable();
        setTurn(false);
        gameClient.sendTurn(e.getActionCommand());
    }

    private void createPlayerList(GameContainer container) {
        for(int i = 0; i < players; i++) {
            PlayerPanelGame playerPanel = new PlayerPanelGame(i + 1, container.getWidth(), container.getHeight());
            if(i > 0) {
                float y = playerList.get(i - 1).getY() + playerList.get(i - 1).getHeight();
                playerPanel.setY(y);
            }
            playerPanel.setPlayerName("Player " + (i + 1));
            playerList.add(playerPanel);
        }
    }

    private void createGameboard(GameContainer container) {
        gameboard = new GameBoard(players, container.getWidth(), container.getHeight());
    }

    @Override
    public void leave(GameContainer container, StateBasedGame game) throws SlickException {
        if(host) {
            host = false;
            gameServer.stop();
        }
        gameClient.stop();
        serverIP = "localhost";
        gameboard.reset();
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    public int getTotalPlayers() {
        return players;
    }

    public void setPlayerMovementPieces(List<MovementPiece> movementPieces) {
        movementSelectPanel.setPieces(movementPieces);
    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
        gameboard.setRenderTop(playerNumber);
        playerList.get(playerNumber - 1).setActivePlayer(true);
    }

    public void setTurn(boolean turn) {
        this.turn = turn;
    }

    public void setAllPlayersTurning() {
        for(PlayerPanelGame player : playerList) {
            player.setStatus(PlayerPanelGame.STATUSCODE.TURNING);
        }
    }

    public void mouseDragged(int oldx, int oldy, int newx, int newy) {
        movementSelectPanel.mouseDragged(oldx, oldy, newx, newy);
    }

    public void movePlayer(int player, int location) {
        gameboard.movePlayer(player, location);
    }

    public void moveNeutralShip(int ship, int location) {
        gameboard.moveNeutralShip(ship, location);
    }

    public void doEndGame(int winningPlayer) {
        String winningPlayerName = playerList.get(winningPlayer - 1).getPlayerName();
        String playerName = playerList.get(playerNumber - 1).getPlayerName();
        if(winningPlayer == playerNumber) {
            popup.setMessage("Congratulations " + winningPlayerName + ", you won! Unfortunately, I'm just a string that has " +
                    "been pre-entered, so I cannot give you your victory cookie.");
        } else {
            popup.setMessage("Congratulations " + playerName + ", you lost! " + winningPlayerName + " is the winner.");
        }
        popup.show();
    }

    @Override
    public void mouseClicked(int button, int x, int y, int clickCount) {
        chatBox.mouseClicked(button, x, y, clickCount);
    }

    @Override
    public void keyReleased(int key, char c) {
        // Escape key
        if(key == Input.KEY_ESCAPE) {
            menu.toggle();
        }
    }

    public void setPlayerStatus(int player, PlayerPanelGame.STATUSCODE code) {
        playerList.get(player - 1).setStatus(code);
    }

    public void outputChatMessage(String message) {
        chatBox.addMessageToQueue(message);
    }

    public void sendChatMessage(String message) {
        gameClient.sendChatMessage(message);
    }

	public void doError(String errorMessage) {
		((MainMenu)game.getState(Main.STATE_ID.MAIN_MENU.id)).setError(errorMessage);
		game.enterState(Main.STATE_ID.MAIN_MENU.id);
	}
}
