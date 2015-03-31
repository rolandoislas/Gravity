package com.rolandoislas.gravity.gui;

import com.rolandoislas.gravity.state.MultiplayerLobby;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.gui.TextField;
import java.awt.Font;

/**
 * @author Rolando Islas
 */
public class PlayerPanel {

    private final int x;
    private final int y;
    private final GameContainer container;
    private final MultiplayerLobby lobby;
    private int width;
    private int height;
    private int id;
    private String name = "";
    private Rectangle mainBox;
    private PlayerIcon playerIcon;
    private Rectangle statusIndicator;
    private Color mainBoxColor = new Color(255, 255, 255, 128);
    private Color statusColorReady = Color.green;
    private Color statusColorNotReady = Color.darkGray;
    private Color statusColor = statusColorNotReady;
    private Checkbox checkbox;
    private Label checkboxLabel;
    private boolean activePlayer = false;
    private boolean hasJoined = false;
    private TextField playerName;

    public PlayerPanel(MultiplayerLobby lobby, GameContainer container, int x, int y, int width, int height, int id) {
        this.lobby = lobby;
        this.container = container;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.id = id;
        createBox();
        createPlayerIcon();
        createPlayerName();
        createStatusIndicator();
        createReadyCheckbox();
    }

    private void createStatusIndicator() {
        int posX = x + width - 20;
        int posY = y;
        int width = 20;
        int height = this.height;
        statusIndicator = new Rectangle(posX, posY, width, height);
    }

    private void createReadyCheckbox() {
        int size = 10;
        int x = (int) (this.x + this.width - statusIndicator.getWidth() - size - 10);
        int y = this.y + ((this.height - size) / 2);
        checkbox = new Checkbox(x, y, size);

        checkboxLabel = new Label("Ready");
        int labelX = x - checkboxLabel.getWidth() - 5;
        int labelY = this.y + ((this.height - checkboxLabel.getHeight()) / 2) - 1;
        checkboxLabel.setPosition(labelX, labelY);
    }

    private void createPlayerName() {
        double x = this.x + playerIcon.getWidth() + 20;
        double y = this.y + ((height - 20) / 2);
        if (playerName == null)
        playerName = new TextField(container, new Label("").getFont(), (int)x, (int)y, 100, 20);
        playerName.setBackgroundColor(Color.transparent);
        playerName.setMaxLength(50);
        playerName.setBorderColor(Color.transparent);
        playerName.setTextColor(Color.white);
        playerName.addListener(e -> sendPlayerNameChange());
    }

    private void sendPlayerNameChange() {
        if((!playerName.getText().equals("")) && Keyboard.getEventKey() == Input.KEY_ENTER) {
            lobby.sendPlayerNameChanged(playerName.getText());
        }
    }

    private void createPlayerIcon() {
        int height = this.height - 25;
        int width = height;
        playerIcon = new PlayerIcon(id, width, height);
        double x = this.x + 10;
        double y = this.y + 10;
        playerIcon.setPosition((int)x, (int)y);
    }

    private void createBox() {
        mainBox = new Rectangle((float)x, (float)y, width, height);
    }

    public void setPlayerName(String name) {
        this.name = name;
        playerName.setText(name);
    }

    public void render(Graphics g) {
        if(hasJoined) {
            renderBox(g);
            renderPlayerIcon(g);
            renderPlayerName(g);
            renderCheckbox(g);
            renderStatusIndicator(g);
        }
    }

    private void renderStatusIndicator(Graphics g) {
        g.setColor(statusColor);
        g.fill(statusIndicator);
    }

    private void renderCheckbox(Graphics g) {
        if(activePlayer) {
            checkbox.render(g);
            checkboxLabel.render(g);
        }
    }

    private void renderPlayerName(Graphics g) {
        g.setColor(Color.white);
        playerName.render(container, g);
    }

    private void renderPlayerIcon(Graphics g) {
        playerIcon.render(g);
    }

    private void renderBox(Graphics g) {
        g.setColor(mainBoxColor);
        g.draw(mainBox);
    }

    public void setActivePlayer(boolean activePlayer) {
        this.activePlayer = activePlayer;
        playerName.setAcceptingInput(true);
    }

    public void reset() {
        activePlayer = false;
        hasJoined = false;
        checkbox.reset();
        playerName.setAcceptingInput(false);
    }

    public void setPlayerState(boolean state) {
        hasJoined = state;
    }

    public void setStatus(boolean status) {
        if(status) {
            statusColor = statusColorReady;
        } else {
            statusColor = statusColorNotReady;
        }
    }

    public Checkbox getCheckbox() {
        return checkbox;
    }
}
