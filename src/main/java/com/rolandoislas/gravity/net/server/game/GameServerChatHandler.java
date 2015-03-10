package com.rolandoislas.gravity.net.server.game;

import com.rolandoislas.gravity.net.client.game.GameClientDecoder;
import com.rolandoislas.gravity.net.common.NetUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author Rolando.
 */
public class GameServerChatHandler extends ChannelInboundHandlerAdapter {

    private GameServerPlayerHandler playerHandler;

    public GameServerChatHandler(GameServerPlayerHandler playerHandler) {
        this.playerHandler = playerHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String message = NetUtil.byteBufToString((ByteBuf) msg);
        System.out.println("Server C: " + message);
        String code = message.equals("") ? "" : message.substring(0, 2);
        if(code.equals(GameServerDecoder.CODE_CHAT)) {
            handleMessage(ctx, message.substring(5));
        } else {
            ctx.fireChannelRead(NetUtil.stringToByteBuf(message));
        }
    }

    private void handleMessage(ChannelHandlerContext ctx, String message) {
        if(isCommand(message)) {
            interpretCommand(ctx, message.substring(1));
        } else {
            sendChatMessage(ctx, message);
        }
    }

    private void sendChatMessage(ChannelHandlerContext ctx, String message) {
        int playerNumber = GameServerPlayerHandler.getPlayerFromChannel(ctx);
        String playerName = playerHandler.getPlayerName(playerNumber);
        String sendingMessage = playerName + ": " + message;
        String messageLength = String.format("%03d", sendingMessage.length());
        GameServerPlayerHandler.sendMessage(GameClientDecoder.CODE_CHAT + messageLength + sendingMessage);
    }

    private void interpretCommand(ChannelHandlerContext ctx, String message) {
        // TODO
    }

    private boolean isCommand(String message) {
        return message.substring(0, 1).contains("/");
    }

    public static void broadcastMessage(String message) {
        String sendingMessage = "SERVER: " + message;
        String messageLength = String.format("%03d", sendingMessage.length());
        GameServerPlayerHandler.sendMessage(GameClientDecoder.CODE_CHAT + messageLength + sendingMessage);
    }
}
