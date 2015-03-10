package com.rolandoislas.gravity.gui;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

/**
 * @author Rolando Islas
 */
public class PlayerPanel {

    private final int x;
    private final int y;
    private int width;
    private int height;
    private int id;
    private String name = "";
    private Rectangle mainBox;
    private PlayerIcon playerIcon;
    private Label playerLabel;
    private Rectangle statusIndicator;
    private Color mainBoxColor = new Color(255, 255, 255, 128);
    private Color statusColorReady = Color.green;
    private Color statusColorNotReady = Color.darkGray;
    private Color statusColor = statusColorNotReady;
    private Checkbox checkbox;
    private Label checkboxLabel;
    private boolean activePlayer = false;
    private boolean hasJoined = false;

    public PlayerPanel(int x, int y, int width, int height, int id) {
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
        playerLabel = new Label(name);
        double x = this.x + playerIcon.getWidth() + 20;
        double y = this.y + ((height - playerLabel.getHeight()) / 2);
        playerLabel.setPosition((int)x, (int)y);
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
        createPlayerName();
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
        playerLabel.render(g);
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
    }

    public void reset() {
        activePlayer = false;
        hasJoined = false;
        checkbox.reset();
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
