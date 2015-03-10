package com.rolandoislas.gravity.net.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

/**
 * @author Rolando Islas
 */
public class NetUtil {

    public static ByteBuf stringToByteBuf(String message) {
        return Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
    }

    public static String byteBufToString(ByteBuf message) {
        String string = "";
        while(message.isReadable()) {
            string += (char) message.readByte();
        }
        return string;
    }

}
