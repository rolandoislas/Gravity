package com.rolandoislas.gravity.state;

import com.rolandoislas.gravity.Main;
import com.rolandoislas.gravity.gui.Button;
import com.rolandoislas.gravity.gui.Label;
import com.rolandoislas.gravity.gui.PlayerPanel;
import com.rolandoislas.gravity.net.client.lobby.LobbyClientInitializer;
import com.rolandoislas.gravity.net.server.lobby.LobbyServerDecoder;
import com.rolandoislas.gravity.net.server.lobby.LobbyServerInitializer;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * @author Rolando Islas
 */
public class MultiplayerLobby extends BasicGameState {

    private static String ip;
    private final int id;
    private int sidebarWidth = 300;
    private Rectangle sidebarPanel;
    private int chatboxHeight = 200;
    private int chatboxWidth = Main.getWidth() - sidebarWidth;
    private ArrayList<PlayerPanel> playerList = new ArrayList<PlayerPanel>();
    private Rectangle playerListPanel;
    private Rectangle chatPanel;
    private int sidebarPosX = Main.getWidth() - sidebarWidth;
    private Button backButton;
    private Label optionLabel;
    private Color sidebarColor = new Color(0, 0, 0, 200);
    private Color chatboxColor = new Color(0, 0, 0, 200);
    private Color backButtonColor = Color.gray;
    private Color borderColor = Color.white;
    private Color backButtonColorFocused = Color.white;
    private Color backButtonTextColor = Color.black;
    private Color playerPanelColor = new Color(0, 0, 0, 200);
    private int playerPanelWidth = Main.getWidth() - sidebarWidth - 20;
    private static boolean host = false;
    private LobbyServerInitializer lobbyServer;
    private LobbyClientInitializer lobbyClient;
    private boolean netFailed = false;
    private boolean gameStart = false;
    private StateBasedGame game;

    public MultiplayerLobby(int id) {
        this.id = id;
    }

    public static void setHost(boolean host) {
        MultiplayerLobby.host = host;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        createSidebarPanel(game);
        createChatPanel();
        createPlayerListPanel();
    }

    private void createPlayerListPanel() {
        playerListPanel = new Rectangle(0, 0, Main.getWidth() - sidebarWidth, Main.getHeight() - chatboxHeight);
        int x = 10;
        int y = 10;
        for(int i = 0; i < 4; i++) {
            int playerPanelHeight = 100;
            y = y + ((i==0) ? 0 : playerPanelHeight + 10);
            PlayerPanel playerInfoPanel = new PlayerPanel(x, y, playerPanelWidth, playerPanelHeight, i + 1);
            playerInfoPanel.setPlayerName("Player " + (i + 1));
            playerList.add(playerInfoPanel);
        }
    }

    private void createChatPanel() {
        chatPanel = new Rectangle(0, Main.getHeight() - chatboxHeight, chatboxWidth, chatboxHeight);
    }

    private void createSidebarPanel(StateBasedGame game) {
        int sidebarHeight = Main.getHeight();
        int sidebarPosY = 0;
        sidebarPanel = new Rectangle(sidebarPosX, sidebarPosY, sidebarWidth, sidebarHeight);

        backButton = new Button("Exit");
        backButton.addClickAction(e -> doExit());
        int sidebarButtonWidth = 150;
        backButton.setWidth(sidebarButtonWidth);
        backButton.setShapeColor(backButtonColor, "blur");
        backButton.setShapeColor(backButtonColorFocused, "focus");
        backButton.setTextColor(backButtonTextColor);
        backButton.setPosition(sidebarPosX + ((sidebarWidth - sidebarButtonWidth) / 2), sidebarPosY + sidebarHeight - 50);

        optionLabel = new Label("Options");
        optionLabel.setFont("Helvetica", Font.BOLD, 16);
        optionLabel.setPosition(sidebarPosX + ((sidebarWidth - optionLabel.getWidth()) / 2), 5);
    }

	private void doExit() {
		if (host)
			lobbyClient.sendShutdownMessage();
		game.enterState(Main.STATE_ID.MAIN_MENU.id);
	}

	@Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        MainMenu.background.render();
        renderChatbox(g);
        renderSidebar(g);
        renderPlayers(g);
    }

    private void renderPlayers(Graphics g) {
        g.setColor(playerPanelColor);
        g.fill(playerListPanel);
        for (PlayerPanel aPlayerList : playerList) {
            aPlayerList.render(g);
        }
    }

    private void renderChatbox(Graphics g) {
        g.setColor(borderColor);
        g.draw(chatPanel);
        g.setColor(chatboxColor);
        g.fill(chatPanel);
        optionLabel.render(g);
    }

    private void renderSidebar(Graphics g) {
        g.setColor(borderColor);
        g.draw(sidebarPanel);
        g.setColor(sidebarColor);
        g.fill(sidebarPanel);
        backButton.render(g);
        optionLabel.render(g);
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        updateSidebar();
        updatePlayers();
    }

    private void updatePlayers() {
    }

    private void updateSidebar() {
    }

    public static void setServer(String newip) {
        ip = newip;
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) {
        this.game = game;
        if(host) {
            startServer();
        }
        startClient();
    }

    private void connectToLobby() {
        do {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while(!netFailed && lobbyClient.channel() == null);
        if(!netFailed) {
            lobbyClient.sendMessage(LobbyServerDecoder.CODE_CONNECTION);
            lobbyClient.sendMessage(LobbyServerDecoder.CODE_GAMESTATE);
        }
    }

    private void startClient() {
        Runnable client = () -> {
            try {
                lobbyClient = new LobbyClientInitializer(this, ip, LobbyServerInitializer.port);
                lobbyClient.run();
            } catch (Exception e) {
                e.printStackTrace();
                netFailed = true;
                doError("Failed to join server.");
            }
        };
        new Thread(client).start();
        connectToLobby();
    }

    private void startServer() {
        Runnable server = () -> {
            try {
                lobbyServer = new LobbyServerInitializer();
                lobbyServer.run();
            } catch (Exception e) {
                e.printStackTrace();
                netFailed = true;
                doError("Failed to create server. Error: " + e.getLocalizedMessage());
            }
        };
        new Thread(server).start();
    }

    @Override
    public void leave(GameContainer container, StateBasedGame game) {
        if(host) {
            host = false;
            lobbyServer.stop();
        }
        lobbyClient.stop();
        resetPlayerPanels();
        netFailed = false;
    }

    private void resetPlayerPanels() {
        for (PlayerPanel panel : playerList) {
            panel.reset();
        }
    }

    public void updatePlayerState(int player, boolean state) {
        playerList.get(player - 1).setPlayerState(state);
    }

    public void updatePlayerStatus(int player, boolean status) {
        playerList.get(player - 1).setStatus(status);
    }

    public void activatePlayerCard(int playerNumber) {
        playerList.get(playerNumber - 1).setActivePlayer(true);
        addCheckboxListener(playerNumber);
    }

    private void addCheckboxListener(int player) {
        playerList.get(player - 1).getCheckbox().addClickAction(e ->
                sendStatusUpdate(Boolean.parseBoolean(e.getActionCommand()))
        );
    }

    private void sendStatusUpdate(boolean status) {
        int intStatus;
        if(status) {
            intStatus = 1;
        } else {
            intStatus = 0;
        }
        String message = LobbyServerDecoder.CODE_STATUS_CHANGE + "0" + intStatus;
        lobbyClient.sendMessage(message);
    }

    public void startGame(int players) {
        gameStart = true;
        try {
            // Delay in case of player indecision.
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(gameStart) {
            Game gameState = (Game) game.getState(Main.STATE_ID.GAME.id);
            gameState.setServer(ip);
            gameState.setPlayers(players);
            if(host) {
                gameState.setHost(true);
            }
            game.enterState(Main.STATE_ID.GAME.id);
        }
    }

    public void stopStart() {
        gameStart = false;
    }

    public void doError(String message) {
        doError(message, false);
    }

	public void doError(String message, boolean checkHost) {
        if ((checkHost && !host) || !checkHost)
		    ((MainMenu)game.getState(Main.STATE_ID.MAIN_MENU.id)).setError(message);
		game.enterState(Main.STATE_ID.MAIN_MENU.id);
	}
}
