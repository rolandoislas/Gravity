package com.rolandoislas.gravity.net.server.game;

import com.rolandoislas.gravity.gui.PlayerPanelGame;
import com.rolandoislas.gravity.net.common.NetUtil;
import com.rolandoislas.gravity.net.client.game.GameClientDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.HashMap;
import java.util.Map;

public class GameServerPlayerHandler extends ChannelInboundHandlerAdapter {

    public static ChannelGroup recipients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static Map<Integer, Map<String, Object>> players = new HashMap<>();

    static {
        structurePlayerData();
    }

    private static void structurePlayerData() {
        for(int i = 0; i < 4; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("joined", false);
            map.put("channel", null);
            map.put("secret", null);
            map.put("turnCode", null);
            map.put("location", 0);
            players.put(i, map);
        }
    }

    public static void setPlayerSecrets(Map<Integer, String> secrets) {
        for(int i = 0; i < secrets.size(); i++) {
            GameServerPlayerHandler.players.get(i).put("secret", secrets.get(i));
        }
    }

    public static Map<Integer, Map<String, Object>> getPlayers() {
        return players;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        String message = NetUtil.byteBufToString(in);
        System.out.println("Server: " + message);
        handleMessage(ctx, message);
    }

    private void handleMessage(ChannelHandlerContext ctx, String message) {
        String code = message.equals("") ? "" : message.substring(0, 2);
        switch (code) {
            case GameServerDecoder.CODE_CONNECTION :
                if(!sendNewConnectionResponse(ctx, message)) {
                    break;
                }
                activatePlayer(ctx);
                break;
            case GameServerDecoder.CODE_GAMESTATE :
                sendGameState(ctx);
                checkGameStart();
                break;
            default :
                ctx.fireChannelRead(NetUtil.stringToByteBuf(message));
                break;
        }
    }

    private void checkGameStart() {
        int totalPlayers = 0;
        for(int i = 0; i < players.size(); i++) {
            // Check if player has secret. If they do and have not joined, return.
            if(players.get(i).get("secret") != null && !(boolean)players.get(i).get("joined")) {
                return;
            }
            // Check if player has secret. No secret defines an empty player slot.
            if(players.get(i).get("secret") == null) {
                break;
            }
            totalPlayers++;
        }
        GameServerLogicHandler.startGame(totalPlayers);
    }

    private void sendGameState(ChannelHandlerContext ctx) {
        String state = "";
        for(int i = 0; i < players.size(); i++) {
            String playerState = (boolean) (players.get(i).get("joined")) ? "1" : "0";
            state += "0" + (i + 1) + "0" + playerState;
        }
        sendMessage(ctx, GameClientDecoder.CODE_GAMESTATE + state);
    }

    private void activatePlayer(ChannelHandlerContext ctx) {
        int player = getPlayerFromChannel(ctx);
        players.get(player - 1).put("joined", true);
        recipients.add(ctx.channel());
        sendMessage(GameClientDecoder.CODE_STATUS_CHANGE + "0" + player + "0" + PlayerPanelGame.STATUSCODE.CONNECTED.code);
    }

    public static int getPlayerFromChannel(ChannelHandlerContext ctx) {
        for(int i = 0; i < players.size(); i++) {
            if(ctx.channel().equals(players.get(i).get("channel"))) {
                return i + 1;
            }
        }
        return -1;
    }

    private boolean sendNewConnectionResponse(ChannelHandlerContext ctx, String message) {
        String secret = message.substring(2);
        for(int i = 0; i < players.size(); i++) {
            String savedSecret = (String) players.get(i).get("secret");
            if(savedSecret.equals(secret)) {
                sendMessage(ctx, GameClientDecoder.CODE_CONNECTION + "0" + (i + 1));
                players.get(i).put("channel", ctx.channel());
                return true;
            }
        }
        sendMessage(ctx, GameClientDecoder.CODE_CONNECTION + "00");
        return false;
    }

    public static void sendMessage(String message) {
        sendMessage((ChannelHandlerContext)null, message);
    }

    public static void sendMessage(Channel channel, String message) {
        sendMessage(null, channel, message);
    }

    private static void sendMessage(ChannelHandlerContext ctx, String message) {
        sendMessage(ctx, null, message);
    }

    public static void sendMessage(ChannelHandlerContext ctx, Channel channel, String message) {
        if(!message.endsWith("\n")) {
            message += "\n";
        }
        ByteBuf msg = NetUtil.stringToByteBuf(message);
        if(ctx == null) {
            if(channel == null) {
                recipients.writeAndFlush(msg);
            } else {
                channel.writeAndFlush(msg);
            }

        } else {
            ctx.writeAndFlush(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
		switch (cause.getMessage()) {
			case "An existing connection was forcibly closed by the remote host" :
				sendMessage(GameClientDecoder.CODE_ERROR + GameClientDecoder.ERROR_CODE.DISCONNECTED.code);
				break;
		}
        ctx.close();
    }

    public String getPlayerName(int playerNumber) {
        // TODO actually get and store player names
        return "Player " + playerNumber;
    }
}
