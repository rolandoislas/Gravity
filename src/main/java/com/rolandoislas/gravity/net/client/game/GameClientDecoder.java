package com.rolandoislas.gravity.net.client.game;

import com.rolandoislas.gravity.net.common.NetUtil;
import com.rolandoislas.gravity.net.server.game.GameServerChatHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author Rolando.
 */
public class GameClientDecoder extends ByteToMessageDecoder {

    public static final String CODE_CONNECTION = "01";
    public static final String CODE_STATUS_CHANGE = "02";
    public static final String CODE_GAMESTATE = "03";
    public static final String CODE_MOVEMNET_PIECES = "04";
    public static final String CODE_TURN_START = "05";
    public static final String CODE_MOVE_PLAYER = "06";
    public static final String CODE_NEUTRAL_SHIP_MOVE = "07";
    public static final String CODE_END_GAME = "08";
    public static final String CODE_CHAT = "09";
	public static final String CODE_ERROR = "10";
	public enum ERROR_CODE {
		DISCONNECTED("01", "A player has disconnected.");
		public String code;
		public String message;
		private ERROR_CODE(String code, String message) {
			this.code = code;
			this.message = message;
		}
	}
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
                bytesOut = 4;
                break;
            case CODE_GAMESTATE :
                bytesOut = 18;
                break;
            case CODE_STATUS_CHANGE :
                bytesOut = 6;
                break;
            case CODE_MOVEMNET_PIECES :
                bytesOut = 32;
                break;
            case CODE_TURN_START :
                bytesOut = 2;
                break;
            case CODE_MOVE_PLAYER :
                bytesOut = 6;
                break;
            case CODE_NEUTRAL_SHIP_MOVE :
                bytesOut = 6;
                break;
            case CODE_END_GAME :
                bytesOut = 4;
                break;
            case CODE_CHAT :
                if(in.readableBytes() < 5) {
                    runCheck = true;
                } else {
                    checkChatMessageEnd(copy);
                }
                break;
            case CODE_ERROR :
                bytesOut = 4;
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
