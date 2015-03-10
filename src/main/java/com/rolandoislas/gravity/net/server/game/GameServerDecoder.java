package com.rolandoislas.gravity.net.server.game;

import com.rolandoislas.gravity.net.common.NetUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author Rolando.
 */
public class GameServerDecoder extends ByteToMessageDecoder {

    public static final String CODE_CONNECTION = "01";
    public static final String CODE_GAMESTATE = "02";
    public static final String CODE_TURN = "03";
    public static final String CODE_CHAT = "04";
    private boolean runCheck = true;
    private int bytesOut;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(runCheck) {
            checkCode(in);
        }
        if (in.readableBytes() == bytesOut) {
            runCheck = true;
            out.add(in.readBytes(bytesOut));
        }
    }

    private void checkCode(ByteBuf in) {
        if (in.readableBytes() < 2) {
            return;
        }
        ByteBuf copy = in.copy();
        String code = "";
        for(int i = 0; i < 2; i++) {
            code += (char)copy.readByte();
        }
        runCheck = false;
        switch (code) {
            case CODE_CONNECTION :
                bytesOut = 12;
                break;
            case CODE_GAMESTATE :
                bytesOut = 2;
                break;
            case CODE_TURN :
                bytesOut = 7;
                break;
            case CODE_CHAT:
                if(in.readableBytes() < 5) {
                    runCheck = true;
                } else {
                    checkChatMessageEnd(copy);
                }
                break;
            default :
                bytesOut = in.readableBytes();
                break;
        }
    }

    private void checkChatMessageEnd(ByteBuf message) {
        int messageLength = Integer.parseInt(NetUtil.byteBufToString(message.copy()).substring(0, 3));
        if(message.readableBytes() == messageLength + 3) {
            bytesOut = messageLength + 5;
            runCheck = false;
        }
        runCheck = true;
    }

}
