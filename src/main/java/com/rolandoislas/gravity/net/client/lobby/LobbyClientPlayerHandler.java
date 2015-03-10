package com.rolandoislas.gravity.net.client.lobby;

import com.rolandoislas.gravity.net.common.NetUtil;
import com.rolandoislas.gravity.net.client.game.GameClientInitializer;
import com.rolandoislas.gravity.state.MultiplayerLobby;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author Rolando Islas
 */
public class LobbyClientPlayerHandler extends ChannelInboundHandlerAdapter {

    private MultiplayerLobby lobby;

    public LobbyClientPlayerHandler(MultiplayerLobby lobby) {
        this.lobby = lobby;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String message = NetUtil.byteBufToString((ByteBuf) msg);
        //System.out.println("Client: " + message);
        String code = message.equals("") ? "" : message.substring(0, 2);
        switch(code) {
            case LobbyClientDecoder.CODE_CONNECTION :
                setPlayerNumber(message);
                break;
            case LobbyClientDecoder.CODE_GAMESTATE :
                updateGamestate(message);
                break;
            case LobbyClientDecoder.CODE_STATE_CHANGE :
                updatePlayerState(message);
                break;
            case LobbyClientDecoder.CODE_STATUS_CHANGE :
                updatePlayerStatus(message);
                break;
            case LobbyClientDecoder.CODE_GAME_START :
                startGame(message);
                break;
            case LobbyClientDecoder.CODE_SECRET :
                setSecret(message);
                break;
            default :
                ctx.fireChannelRead(NetUtil.stringToByteBuf(message));
                break;
        }
    }

    private void setSecret(String message) {
        GameClientInitializer.setSecret(message.substring(2));
    }

    private void startGame(String message) {
        boolean status = Integer.parseInt(message.substring(2, 4)) == 1;
        int players = Integer.parseInt(message.substring(4, 6));
        if(status) {
            lobby.startGame(players);
        } else {
            lobby.stopStart();
        }
    }

    private void updatePlayerStatus(String message) {
        int player = Integer.parseInt(message.substring(2, 4));
        boolean status = Integer.parseInt(message.substring(4, 6)) == 1;
        lobby.updatePlayerStatus(player, status);
    }

    private void updatePlayerState(String message) {
        int player = Integer.parseInt(message.substring(2, 4));
        boolean state = Integer.parseInt(message.substring(4, 6)) == 1;
        lobby.updatePlayerState(player, state);
    }

    private void updateGamestate(String message) {
        for(int i = 0; i < 4; i++) {
            int mod = 6 * i;
            int player = Integer.parseInt(message.substring(2 + mod, 4 + mod));
            boolean state = Integer.parseInt(message.substring(4 + mod, 6 + mod)) == 1;
            boolean status = Integer.parseInt(message.substring(6 + mod, 8 + mod)) == 1;
            lobby.updatePlayerState(player, state);
            lobby.updatePlayerStatus(player, status);
        }
    }

    private void setPlayerNumber(String message) {
        lobby.activatePlayerCard(Integer.parseInt(message.substring(2, 4)));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


}
