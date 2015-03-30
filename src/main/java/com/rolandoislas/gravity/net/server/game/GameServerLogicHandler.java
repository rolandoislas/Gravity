package com.rolandoislas.gravity.net.server.game;

import com.rolandoislas.gravity.gui.PlayerPanelGame;
import com.rolandoislas.gravity.logic.MovementPiece;
import com.rolandoislas.gravity.net.common.NetUtil;
import com.rolandoislas.gravity.net.client.game.GameClientDecoder;
import com.rolandoislas.gravity.world.GameBoard;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;

/**
 * @author Rolando.
 */
public class GameServerLogicHandler extends ChannelInboundHandlerAdapter {

    // player -> 6 pieces
    private static List<List<MovementPiece>> movementPieces = new ArrayList<>();
    private static int totalPlayers;
    private static int turnPosition = 0;
    private static int roundPosition = 0;
    private static List<Integer> turnList = new ArrayList<>();
    private static int[] neutralShips;
    private boolean gameOver = false;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        String message = NetUtil.byteBufToString(in);
        System.out.println("Server L: " + message);
        handleMessage(ctx, message);
    }

    private void handleMessage(ChannelHandlerContext ctx, String message) {
        String code = message.equals("") ? "" : message.substring(0, 2);
        switch (code) {
            case GameServerDecoder.CODE_TURN :
                registerPlayerTurn(ctx, message);
                checkEndOfTurn();
                break;
            default :
                ctx.fireChannelRead(NetUtil.stringToByteBuf(message));
                break;
        }
    }

    private void checkEndOfTurn() {
        Map<Integer, Map<String, Object>> players = GameServerPlayerHandler.getPlayers();
        for(int i = 0; i < totalPlayers; i++) {
            if(players.get(i).get("turnCode") == null) {
                return;
            }
        }
        turnPosition++;
        // Create a new turn list based on played movement alphabet prefix
        createTurnListFromAlphabet();
        // Send calculated movements to players
        sendPlayerMovements();
        // Clear turns
        resetPlayerTurns();
        // Check if round has ended
        checkRoundStart();
        // Victory conditions
        checkVictoryConditions();
        // Start new turn
        if(!gameOver) {
            sendTurnNotification();
        }
    }

    private void checkVictoryConditions() {
        for (int i = 0; i < totalPlayers; i++) {
            // Check if player is at end of map
            if ((int)GameServerPlayerHandler.getPlayers().get(i).get("location") == GameBoard.getTotalLocations()) {
                doGameEnd();
                break;
            }
        }
    }

    private void checkRoundStart() {
        if(turnPosition == 6) { // All turn pieces have been exhausted
            turnPosition = 0;
            roundPosition++;
            if(roundPosition == 6) { // All round have been exhausted
                doGameEnd();
            } else {
                // Create and send new movement pieces
                createPlayerMovementPieces();
                sendPlayerMovementPieces();
            }
        }
    }

    private void doGameEnd() {
        gameOver = true;
        List<int[]> players = getShipDistancesFromPoint(GameBoard.getTotalLocations() - 1);
        GameServerPlayerHandler.sendMessage(GameClientDecoder.CODE_END_GAME + "0" + players.get(0)[0]);
        turnPosition = 0;
        roundPosition = 0;
    }

    private void createTurnListFromAlphabet() {
        turnList.clear();
        Collection<String> alphaPrefixes = new TreeSet<>();
        Map<Integer, Map<String, Object>> players = GameServerPlayerHandler.getPlayers();
        for(int i = 0; i < totalPlayers; i++) {
            alphaPrefixes.add(((MovementPiece)players.get(i).get("turnCode")).getAlphaPrefix());
        }
        for(int i = 0; i < totalPlayers; i++) {
            for (String prefix : alphaPrefixes) {
                if(prefix.equals(((MovementPiece)players.get(i).get("turnCode")).getAlphaPrefix())) {
                    turnList.add(i + 1);
                }
            }
        }
    }

    private void createTurnListFromLocation() {
        List<int[]> shipDistances = getShipDistancesFromPoint(0);
        turnList.clear();
        for(int i = 0; i < totalPlayers; i++) {
            turnList.add(shipDistances.get(i)[0]);
        }
    }

    private void resetPlayerTurns() {
        Map<Integer, Map<String, Object>> players = GameServerPlayerHandler.getPlayers();
        for(int i = 0; i< totalPlayers; i++) {
            players.get(i).put("turnCode", null);
        }
    }

    private void sendPlayerMovements() {
        Map<Integer, Map<String, Object>> players = GameServerPlayerHandler.getPlayers();
        for(int i = 0; i < totalPlayers; i++) {
            int player = turnList.get(i);
            MovementPiece mp = (MovementPiece) players.get(player - 1).get("turnCode");
            int playerLocation = (int) players.get(player - 1).get("location");
            switch (mp.getType()) {
                case REPULSE:
                    sendRepulseMovement(player, playerLocation, mp);
                    break;
                case ATTRACT:
                    sendAttractMovement(player, playerLocation, mp);
                    break;
                case SHIFT:
                    sendShiftMovement(player, playerLocation, mp);
                    break;
                default:
                    break;
            }
        }
    }

    private void sendShiftMovement(int player, int playerLocation, MovementPiece mp) {
        Map<Integer, Map<String, Object>> players = GameServerPlayerHandler.getPlayers();
        List<int[]> shipDistances = getShipDistancesFromPoint(playerLocation);
        for(int i = 0; i < totalPlayers; i++) {
            int newPlayer = shipDistances.get(i)[0];
            if(player != newPlayer) {
                int newPlayerPosition = (int) players.get(newPlayer - 1).get("location");
                sendAttractMovement(newPlayer, newPlayerPosition, mp, playerLocation, true);
            }
        }
        for(int i = 0; i < neutralShips.length; i++) {
            sendAttractMovement(i + 1, neutralShips[i], mp, playerLocation, false);
        }
    }

    private void sendAttractMovement(int player, int playerLocation, MovementPiece mp) {
        sendAttractMovement(player, playerLocation, mp, null, true);
    }

    private void sendAttractMovement(int player, int playerLocation, MovementPiece mp, Integer gravity , boolean isPlayerShip) {
        if(gravity == null) {
            gravity = getGravity(player);
        }
        int newPosition = 0;
        // Gravity behind player
        if(gravity < playerLocation) {
            newPosition = playerLocation - mp.getPlaces();
            while(!isPlayerLocationEmpty(newPosition)) {
                newPosition--;
            }
        }
        // Gravity in front of player
        if(gravity > playerLocation) {
            newPosition = playerLocation + mp.getPlaces();
            while(!isPlayerLocationEmpty(newPosition)) {
                newPosition++;
            }
        }
        // Gameboard bound checking
        if(newPosition < 0) {
            newPosition = 0;
        }
        if(newPosition > GameBoard.getTotalLocations() - 1) {
            newPosition = GameBoard.getTotalLocations() - 1;
        }
        // Set player location.
        if(isPlayerShip) {
            Map<Integer, Map<String, Object>> players = GameServerPlayerHandler.getPlayers();
            players.get(player - 1).put("location", newPosition);
        } else { // Set neutral ship position
            neutralShips[player - 1] = newPosition;
        }
        // Send move to all players.
        String positionString = String.format("%02d", newPosition);
        if(isPlayerShip) {
            GameServerPlayerHandler.sendMessage(GameClientDecoder.CODE_MOVE_PLAYER + "0" + player + positionString);
            GameServerChatHandler.broadcastMessage("Player " + player + " moved to slot " + newPosition + ".");
        } else { // Send neutral ship movement
            GameServerPlayerHandler.sendMessage(GameClientDecoder.CODE_NEUTRAL_SHIP_MOVE + "0" + player + positionString);
        }
    }

    private void sendRepulseMovement(int player, int playerLocation, MovementPiece mp) {
        int gravity = getGravity(player);
        // Gravity behind player
        if(gravity < playerLocation) {
            sendAttractMovement(player, playerLocation, mp, 1000000, true);
        }
        // Gravity in front of player
        if(gravity > playerLocation) {
            sendAttractMovement(player, playerLocation, mp, 0, true);
        }
        /*
        int gravity = getGravity(player);
        int newPosition = 0;
        // Gravity behind player
        if(gravity < playerLocation) {
            newPosition = playerLocation + mp.getPlaces();
            while(!isPlayerLocationEmpty(newPosition)) {
                newPosition++;
            }
        }
        // Gravity in front of player
        if(gravity > playerLocation) {
            newPosition = playerLocation - mp.getPlaces();
            while(!isPlayerLocationEmpty(newPosition)) {
                newPosition--;
            }
        }
        // Gameboard bound checking
        if(newPosition < 0) {
            newPosition = 0;
        }
        if(newPosition > GameBoard.getTotalLocations() - 1) {
            newPosition = GameBoard.getTotalLocations() - 1;
        }
        // Set player location.
        Map<Integer, Map<String, Object>> players = GameServerPlayerHandler.getPlayers();
        players.get(player - 1).put("location", newPosition);
        // Send move to all players.
        String positionString = String.format("%02d", newPosition);
        GameServerPlayerHandler.sendMessage(GameClientDecoder.CODE_MOVE_PLAYER + "0" + player + positionString);
        */
    }

    private boolean isPlayerLocationEmpty(int position) {
        Map<Integer, Map<String, Object>> players = GameServerPlayerHandler.getPlayers();
        for(int i = 0; i < totalPlayers; i++) {
            if((int)(players.get(i).get("location")) == position || neutralShips[0] == position || neutralShips[1] == position) {
                return false;
            }
        }
        return true;
    }

    private int getGravity(int player) {
        Map<Integer, Map<String, Object>> players = GameServerPlayerHandler.getPlayers();
        int playerLocation = (int)(players.get(player - 1).get("location"));
        // Send a big number to force gravity forward.
        if(playerLocation == 0) {
            return 1000000; // One million dollars!
        }
        // Get ship if player is surrounded
        if(!isPlayerLocationEmpty(playerLocation - 1) && !isPlayerLocationEmpty(playerLocation + 1)) {
            boolean backIsNeutralShip = playerLocation - 1 == neutralShips[0] || playerLocation - 1 == neutralShips[1];
            boolean frontIsNeutralShip = playerLocation + 1 == neutralShips[0] || playerLocation + 1 == neutralShips[1];
            // IF player is surrounded by neutral ships send closest player.
            if(backIsNeutralShip && frontIsNeutralShip) {
                return getClosestPlayerShip(playerLocation);
            }
            // Send closest neutral ship.
            return getClosetNeutralShip(playerLocation, true);
        }
        return getClosestShip(playerLocation);
    }

    private int getClosestShip(int playerLocation) {
        int player = getClosestPlayerShip(playerLocation);
        int playerDistance = getLocationsDistance(playerLocation, player);
        int neutral = getClosetNeutralShip(playerLocation);
        int neutralDistance = getLocationsDistance(playerLocation, neutral);
        if(playerDistance < neutralDistance) {
            return player;
        }
        return neutral;
    }

    private int getClosestPlayerShip(int playerLocation) {
        Map<Integer, Map<String, Object>> players = GameServerPlayerHandler.getPlayers();
        List<int[]> distances = getShipDistancesFromPoint(playerLocation);
        int player = distances.get(0)[0];
        int playerDistance = distances.get(0)[1];
        if(playerDistance == 0) {
            player = distances.get(1)[0];
        }
        return (int) players.get(player - 1).get("location");
    }

    private List<int[]> getShipDistancesFromPoint(int playerLocation) {
        Map<Integer, Map<String, Object>> players = GameServerPlayerHandler.getPlayers();
        List<int[]> ships = new ArrayList<>();
        // Add distances to list
        for(int i = 0; i < totalPlayers; i++) {
            int distance = getLocationsDistance(playerLocation, (int) players.get(i).get("location"));
            ships.add(new int[] {i + 1, distance});
        }

        // Sort list low-high
        List<int[]> sortedShips = new ArrayList<>();
        int index = 0;
        for(int[] s : ships) {
            for (int i = index; i < GameBoard.getTotalLocations(); i++) {
                for (int[] ship : ships) {
                    if (i == ship[1]) {
                        index = i;
                        sortedShips.add(ship);
                        break;
                    }
                }
            }
        }
        return sortedShips;
    }

    private int getClosetNeutralShip(int playerLocation) {
        return getClosetNeutralShip(playerLocation, false);
    }

    private int getClosetNeutralShip(int playerLocation, boolean getShipNotNextToPlayer) {
        int closestShip = 0;
        int distance = 1000000;
        for(int i = 0; i < neutralShips.length; i++) {
            int newDistance = getLocationsDistance(playerLocation, neutralShips[i]);
            boolean isShipNextToPlayer = getShipNotNextToPlayer && newDistance == 1;
            if(!isShipNextToPlayer && newDistance < distance) {
                closestShip = i;
                distance = newDistance;
            }
        }
        return neutralShips[closestShip];
    }

    private int getLocationsDistance(int locationOne, int locationTwo) {
        int distance = 0;
        if(locationOne < locationTwo) {
            distance = locationTwo - locationOne;
        } else
        if(locationTwo < locationOne) {
            distance = locationOne - locationTwo;
        }
        return distance;
    }

    private void registerPlayerTurn(ChannelHandlerContext ctx, String message) {
        String turnCode = message.substring(2);
        MovementPiece mp = new MovementPiece(turnCode);
        int player = GameServerPlayerHandler.getPlayerFromChannel(ctx);
        GameServerPlayerHandler.getPlayers().get(player - 1).put("turnCode", mp);
        // update player status
        GameServerPlayerHandler.sendMessage(GameClientDecoder.CODE_STATUS_CHANGE + "0" + player + "0" + PlayerPanelGame.STATUSCODE.TURN_WAITNG.code);
    }

    public static void startGame(int totalPlayers) {
        GameServerLogicHandler.totalPlayers = totalPlayers;
        createPlayerMovementPieces();
        sendPlayerMovementPieces();
        createFirstTurnList();
        createNeutralShips();
        sendNeutralShips();
        sendTurnNotification();
    }

    private static void sendNeutralShips() {
        String shipOneString = String.format("%02d", neutralShips[0]);
        String shipTwoString = String.format("%02d", neutralShips[1]);
        GameServerPlayerHandler.sendMessage(GameClientDecoder.CODE_NEUTRAL_SHIP_MOVE + "01" + shipOneString);
        GameServerPlayerHandler.sendMessage(GameClientDecoder.CODE_NEUTRAL_SHIP_MOVE + "02" + shipTwoString);
    }

    private static void createNeutralShips() {
        Random random = new Random();
        int shipOne = random.nextInt(GameBoard.getTotalLocations() - 1) + 1;
        int shipTwo = random.nextInt(GameBoard.getTotalLocations() - 1) + 1;
        neutralShips = new int[] {shipOne, shipTwo};
    }

    private static void createFirstTurnList() {
        int firstPlayer = new Random().nextInt(totalPlayers) + 1;
        turnList.clear();
        for(int i = firstPlayer; i < totalPlayers + 1; i++) {
            turnList.add(i);
        }
        for(int i = 1; i < firstPlayer; i++) {
            turnList.add(i);
        }
    }

    private static void sendTurnNotification() {
        GameServerPlayerHandler.sendMessage(GameClientDecoder.CODE_TURN_START);
    }

    private static void sendPlayerMovementPieces() {
        int iterator = 0;
        for(Channel channel : GameServerPlayerHandler.recipients) {
            if(iterator < totalPlayers) {
                String playerMovementPieces = "";
                for(MovementPiece mp : movementPieces.get(iterator)) {
                    playerMovementPieces += mp.getCode();
                }
                GameServerPlayerHandler.sendMessage(channel, GameClientDecoder.CODE_MOVEMNET_PIECES + playerMovementPieces);
            }
            iterator++;
        }
    }

    private static void createPlayerMovementPieces() {
        movementPieces.clear();
        for(int i = 0; i < totalPlayers; i++) {
            movementPieces.add(new ArrayList<>());
            for(int j = 0; j < 6; j++) {
                movementPieces.get(i).add(new MovementPiece());
            }
        }
    }
}
