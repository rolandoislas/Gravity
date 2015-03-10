package com.rolandoislas.gravity.net.client.lobby;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author Rolando Islas
 */
public class LobbyClientDecoder extends ByteToMessageDecoder {

    public static final String CODE_CONNECTION = "01";
    public static final String CODE_GAMESTATE = "02";
    public static final String CODE_STATUS_CHANGE = "03";
    public static final String CODE_STATE_CHANGE = "04";
    public static final String CODE_GAME_START = "05";
    public static final String CODE_SECRET = "06";

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
        for(int i =0; i < 2; i++) {
            code += (char)copy.readByte();
        }
        runCheck = false;
        switch (code) {
            case CODE_CONNECTION :
                bytesOut = 4;
                break;
            case CODE_GAMESTATE :
                bytesOut = 26;
                break;
            case CODE_STATUS_CHANGE :
                bytesOut = 6;
                break;
            case CODE_STATE_CHANGE :
                bytesOut = 6;
                break;
            case CODE_GAME_START :
                bytesOut = 6;
                break;
            case CODE_SECRET :
                bytesOut = 12;
                break;
            default :
                bytesOut = in.readableBytes();
                break;
        }
    }

}
