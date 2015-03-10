package com.rolandoislas.gravity.net.client.game;

import com.rolandoislas.gravity.net.common.NetUtil;
import com.rolandoislas.gravity.state.Game;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author Rolando.
 */
public class GameClientChatHandler extends ChannelInboundHandlerAdapter {

    private Game game;

    public GameClientChatHandler(Game game) {
        this.game = game;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String message = NetUtil.byteBufToString((ByteBuf) (msg));
        System.out.println("Client C: " + message);
        String code = message.equals("") ? "" : message.substring(0, 2);
        switch(code) {
            case GameClientDecoder.CODE_CHAT :
                outputMessage(message.substring(5));
                break;
            default :
                ctx.fireChannelRead(NetUtil.stringToByteBuf(message));
                break;
        }
    }

    private void outputMessage(String message) {
        game.outputChatMessage(message);
    }

}
