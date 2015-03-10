package com.rolandoislas.gravity.net.server.lobby;

import com.rolandoislas.gravity.net.common.NetUtil;
import com.rolandoislas.gravity.net.client.lobby.LobbyClientDecoder;
import com.rolandoislas.gravity.net.server.game.GameServerPlayerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Rolando Islas
 */
public class LobbyServerPlayerHandler extends ChannelInboundHandlerAdapter {

    public static ChannelGroup recipients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public static Map<Integer, Map<String, Object>> players = new HashMap<>();

    static {
        structurePlayerData();
    }

    private static void structurePlayerData() {
        for(int i = 0; i < 4; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("joined", false);
            map.put("status", false);
            map.put("channel", null);
            map.put("secret", null);
            players.put(i, map);
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        removePlayer(ctx);
    }

    private void removePlayer(ChannelHandlerContext ctx) {
        try {
            int player = getPlayerFromCurrentChannel(ctx);
            players.get(player - 1).put("joined", false);
            players.get(player - 1).put("status", false);
            players.get(player - 1).put("channel", null);
            players.get(player - 1).put("secret", null);
            recipients.remove(ctx.channel());
            sendStateChangeMessage(player, 0);
            sendStatusUpdateMessage(player, 0);
        } catch (Exception ignored) {
        }
    }

    private void sendStateChangeMessage(int player, int status) {
        sendMessage(LobbyClientDecoder.CODE_STATE_CHANGE + "0" + player + "0" + status);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        String message = NetUtil.byteBufToString(in);
        //System.out.println("Server: " + message);
        handleMessage(ctx, message);
    }

    private void handleMessage(ChannelHandlerContext ctx, String message) {
        String code = message.equals("") ? "" : message.substring(0, 2);
        switch (code) {
            case LobbyServerDecoder.CODE_CONNECTION :
                if(!sendNewConnectionResponse(ctx)) {
                    break;
                }
                createPlayer(ctx);
                sendPlayerSecret(ctx);
                break;
            case LobbyServerDecoder.CODE_GAMESTATE :
                sendGameState(ctx);
                break;
            case LobbyServerDecoder.CODE_STATUS_CHANGE :
                updatePlayerStatus(ctx, message);
                checkGameStart(ctx);
                break;
            default :
                ctx.fireChannelRead(NetUtil.stringToByteBuf(message));
                break;
        }
    }

    private void checkGameStart(ChannelHandlerContext ctx) {
        boolean playersReady = true;
        for(int i = 0; i < players.size(); i++) {
            if(((boolean)players.get(i).get("joined")) && (!(boolean)players.get(i).get("status"))) {
                playersReady = false;
                break;
            }
        }
        String message;
        int totalPlayers = getNewPlayerNumberInt() == 0 ? 4 : getNewPlayerNumberInt() - 1;
        if(playersReady) {
            message = LobbyClientDecoder.CODE_GAME_START + "010" + totalPlayers;
        } else {
            message = LobbyClientDecoder.CODE_GAME_START + "000" + totalPlayers;
        }
        GameServerPlayerHandler.setPlayerSecrets(getPlayerSecretsMap());
        sendMessage(message);
    }

    private void sendPlayerSecret(ChannelHandlerContext ctx) {
        int player = 0;
        try {
            player = getPlayerFromCurrentChannel(ctx);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendMessage(ctx, LobbyClientDecoder.CODE_SECRET + players.get(player - 1).get("secret"));
    }

    private Map<Integer, String> getPlayerSecretsMap() {
        Map<Integer, String> map = new HashMap<>();
        for(int i = 0; i < players.size(); i++) {
            map.put(i, (String) players.get(i).get("secret"));
        }
        return map;
    }

    private void updatePlayerStatus(ChannelHandlerContext ctx, String message) {
        try {
            int player = getPlayerFromCurrentChannel(ctx);
            int status = Integer.parseInt(message.substring(2, 4));
            players.get(player - 1).put("status", status != 0);
            sendStatusUpdateMessage(player, status);
        } catch (Exception ignored) {
        }
    }

    private int getPlayerFromCurrentChannel(ChannelHandlerContext ctx) throws Exception {
        for(int i = 0; i < players.size(); i++) {
            Channel playerChannel = (Channel) players.get(i).get("channel");
            if(playerChannel != null && playerChannel.equals(ctx.channel())) {
                return i + 1;
            }
        }
        throw new Exception();
    }

    private void sendStatusUpdateMessage(int player, int status) {
        String message = LobbyClientDecoder.CODE_STATUS_CHANGE + "0" + player + "0" + status;
        sendMessage(message);
    }

    private void sendMessage(String message) {
        sendMessage(null, message);
    }

    private boolean sendNewConnectionResponse(ChannelHandlerContext ctx) {
        int player = Integer.parseInt(getNewPlayerNumber());
        if(player < 1) {
            sendMessage(ctx, LobbyClientDecoder.CODE_CONNECTION + "00");
            return false;
        }
        // Send player number
        sendMessage(ctx, LobbyClientDecoder.CODE_CONNECTION + getNewPlayerNumber());
        // Notify clients
        sendStateChangeMessage(player, 1);
        return true;
    }

    private void sendMessage(ChannelHandlerContext ctx, String message) {
        if(!message.endsWith("\n")) {
            message += "\n";
        }
        ByteBuf msg = NetUtil.stringToByteBuf(message);
        if(ctx == null) {
            recipients.writeAndFlush(msg);
        } else {
            ctx.writeAndFlush(msg);
        }
    }

    private void createPlayer(ChannelHandlerContext ctx) {
        int playerNumber = Integer.parseInt(getNewPlayerNumber()) - 1;
        players.get(playerNumber).put("joined", true);
        players.get(playerNumber).put("channel", ctx.channel());
        players.get(playerNumber).put("secret", generateSecret(playerNumber));
        recipients.add(ctx.channel());
    }

    private String generateSecret(int seed) {
        Random random = new Random(seed);
        String secret = "";
        for(int i = 0; i < 10; i++) {
            secret += random.nextInt(10);
        }
        return secret;
    }

    private void sendGameState(ChannelHandlerContext ctx) {
        List<String> responseList = new ArrayList<String>() {{
            add(LobbyClientDecoder.CODE_GAMESTATE);
            add(getPlayersStats());
        }};
        String response = "";
        for(String string : responseList) {
            response += string;
        }
        sendMessage(ctx, response);
    }

    private String getPlayersStats() {
        List<String> playerInfo = new ArrayList<>();
        for(int i = 0; i < players.size(); i++) {
            playerInfo.add("0" + (i + 1));
            playerInfo.add((boolean)players.get(i).get("joined") ? "01" : "00");
            playerInfo.add((boolean)players.get(i).get("status") ? "01" : "00");
        }
        String infoString = "";
        for(String string : playerInfo) {
            infoString += string;
        }
        return infoString;
    }

    public String getNewPlayerNumber() {
        int openSlot = 0;
        for(int i = 0; i < players.size(); i++) {
            if(!(boolean)players.get(i).get("joined")) {
                openSlot = i + 1;
                break;
            }
        }
        return "0" + openSlot;
    }

    public int getNewPlayerNumberInt() {
        return Integer.parseInt(getNewPlayerNumber());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
