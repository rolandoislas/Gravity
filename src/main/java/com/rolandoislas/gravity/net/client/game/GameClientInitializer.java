package com.rolandoislas.gravity.net.client.game;

import com.rolandoislas.gravity.net.common.NetUtil;
import com.rolandoislas.gravity.net.server.game.GameServerDecoder;
import com.rolandoislas.gravity.state.Game;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

/**
 * @author Rolando.
 */
public class GameClientInitializer {

    private NioEventLoopGroup workerGroup;
    private Game game;
    private String serverIP;
    private int port;
    private Channel channel;
    private static String secret;

    public GameClientInitializer(Game game, String serverIP, int port) {
        this.game = game;
        this.serverIP = serverIP;
        this.port = port;
    }

    public static void setSecret(String secret) {
        GameClientInitializer.secret = secret;
    }

    public void run() throws InterruptedException {
        workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new DelimiterBasedFrameDecoder(150, true, NetUtil.stringToByteBuf("\n")),
                            new GameClientDecoder(), new GameClientPlayerHandler(game),
                            new GameClientLogicHandler(game), new GameClientChatHandler(game));
                }
            });

            ChannelFuture f = b.connect(serverIP, port).sync();
            channel = f.channel();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public Channel channel() {
        return channel;
    }

    public void stop() {
        workerGroup.shutdownGracefully();
    }

    public void sendMessage(String message) {
        if(!message.endsWith("\n")) {
            message += "\n";
        }
        channel.writeAndFlush(NetUtil.stringToByteBuf(message));
    }

    public void connect() {
        sendMessage(GameServerDecoder.CODE_CONNECTION + secret);
    }

    public void requestGameState() {
        sendMessage(GameServerDecoder.CODE_GAMESTATE);
    }

    public void sendTurn(String code) {
        sendMessage(GameServerDecoder.CODE_TURN + code);
    }

    public void sendChatMessage(String message) {
        String messageLength = String.format("%03d", message.length());
        sendMessage(GameServerDecoder.CODE_CHAT + messageLength + message);
    }
}
