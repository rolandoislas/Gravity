package com.rolandoislas.gravity.net.server.lobby;

import com.rolandoislas.gravity.net.common.NetUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author Rolando Islas
 */
public class LobbyServerDecoder extends ByteToMessageDecoder {

    private int bytesOut;
    private boolean runCheck = true;
    public static final String CODE_CONNECTION = "01";
    public static final String CODE_GAMESTATE = "02";
    public static final String CODE_STATUS_CHANGE = "03";
    public static final String CODE_REQUEST_PLAYERS = "04";
	public static final String CODE_SHUTDOWN = "05";
    public static final String CODE_NAME = "06";

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
                bytesOut = 2;
                break;
            case CODE_GAMESTATE :
                bytesOut = 2;
                break;
            case CODE_STATUS_CHANGE :
                bytesOut = 4;
                break;
            case CODE_REQUEST_PLAYERS :
                bytesOut = 2;
                break;
            case CODE_SHUTDOWN :
                bytesOut = 2;
                break;
            case CODE_NAME :
                if(in.readableBytes() < 5) {
                    runCheck = true;
                } else {
                    checkNameMessageEnd(copy);
                }
                break;
            default :
                bytesOut = in.readableBytes();
                break;
        }
    }

    private void checkNameMessageEnd(ByteBuf message) {
        int messageLength = Integer.parseInt(NetUtil.byteBufToString(message.copy()).substring(0, 2));
        if(message.readableBytes() == messageLength + 2) {
            bytesOut = messageLength + 4;
            runCheck = false;
        }
        runCheck = true;
    }

}
