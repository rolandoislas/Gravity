package com.rolandoislas.gravity.gui;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

/**
 * @author Rolando.
 */
public class PlayerPanelGame {

    public enum STATUSCODE {
        CONNECTED(1),
        TURNING(2),
        CONNECTING(3),
        TURN_WAITNG(4);
        public int code;
        private STATUSCODE(int code) {
            this.code = code;
        }
    }
    private final float panelWidth;
    private final float panelHeight;
    private final int shipDim;
    private Rectangle playerBox;
    private String playerName = "";
    private Label playerLabel;
    private PlayerIcon playerIcon;
    private int id;
    private STATUSCODE statusCode = STATUSCODE.CONNECTING;
    private Label statusLabel;

    public PlayerPanelGame(int id, float containerWidth, float containerHeight) {
        this.id = id;
        panelWidth = (float) (containerWidth * .15);
        panelHeight = (float) (containerHeight * .1);
        shipDim  = (int) (panelWidth * .25);
        createPlayerBox();
        createPlayerName();
        createPlayerIcon();
        createStatusIndicator();
    }

    private void createPlayerIcon() {
        int x = (int) (playerBox.getX() + panelWidth - shipDim - panelWidth * 0.05);
        int y = (int) playerBox.getY();
        playerIcon = new PlayerIcon(id, shipDim, shipDim);
        playerIcon.setPosition(x, y);
    }

    private void createStatusIndicator() {
        String statusString;
        switch(statusCode) {
            case CONNECTED:
                statusString = "Connected";
                break;
            case CONNECTING:
                statusString = "Connecting";
                break;
            case TURNING:
                statusString = "Selecting movement";
                break;
            case TURN_WAITNG:
                statusString = "Waiting for other players";
                break;
            default:
                statusString = "";
                break;
        }
        if(statusLabel == null) {
            statusLabel = new Label(statusString);
        } else {
            statusLabel.setText(statusString);
        }
        int x = (int) (playerBox.getX() + panelWidth * 0.05);
        int y = (int) (playerBox.getY() + playerBox.getHeight() - statusLabel.getHeight() - panelHeight * 0.05);
        statusLabel.setPosition(x, y);
    }

    private void createPlayerName() {
        playerLabel = new Label(playerName);
        playerLabel.setPosition((int)(playerBox.getX() + panelWidth * 0.05), (int)(playerBox.getY() + panelHeight * 0.05));
    }

    private void createPlayerBox() {
        playerBox = new Rectangle(0, 0, panelWidth, panelHeight);
    }


    public void setStatus(STATUSCODE code) {
        statusCode = code;
        createStatusIndicator();
    }

    public void render(Graphics g) {
        renderPlayerBox(g);
        renderPlayerName(g);
        renderPlayerIcon(g);
        renderStatusIndicator(g);
    }

    private void renderStatusIndicator(Graphics g) {
        statusLabel.render(g);
    }

    private void renderPlayerIcon(Graphics g) {
        playerIcon.render(g);
    }

    private void renderPlayerName(Graphics g) {
        playerLabel.render(g);
    }

    private void renderPlayerBox(Graphics g) {
        g.setColor(Color.black);
        g.fill(playerBox);
        g.setColor(Color.gray);
        g.draw(playerBox);
    }

    public float getY() {
        return playerBox.getY();
    }

    public float getHeight() {
        return panelHeight;
    }

    public void setY(float y) {
        playerBox.setY(y);
        createPlayerName();
        createPlayerIcon();
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
        createPlayerName();
    }

    public void setActivePlayer(boolean isActivePlayer) {
        playerIcon.setShipFilled(isActivePlayer);
    }

    public String getPlayerName() {
        return playerName;
    }
}
