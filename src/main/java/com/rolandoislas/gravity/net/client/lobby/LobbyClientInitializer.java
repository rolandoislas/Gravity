package com.rolandoislas.gravity.net.client.lobby;

import com.rolandoislas.gravity.net.common.NetUtil;
import com.rolandoislas.gravity.state.MultiplayerLobby;
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
 * @author Rolando Islas
 */
public class LobbyClientInitializer {

    private MultiplayerLobby lobby;
    private String host;
    private int port;
    private Channel channel;
    private NioEventLoopGroup workerGroup;

    public LobbyClientInitializer(MultiplayerLobby lobby, String host, int port) {
        this.lobby = lobby;
        this.host = host;
        this.port = port;
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
                    // TODO add deliminator handler
                    ch.pipeline().addLast(new DelimiterBasedFrameDecoder(50, true, NetUtil.stringToByteBuf("\n")), new LobbyClientDecoder(), new LobbyClientPlayerHandler(lobby));
                }
            });

            ChannelFuture f = b.connect(host, port).sync();
            channel = f.channel();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public void sendMessage(String msg) {
        if(!msg.endsWith("\n")) {
            msg += "\n";
        }
        channel.writeAndFlush(NetUtil.stringToByteBuf(msg));
    }

    public Channel channel() {
        return channel;
    }

    public void stop() {
        workerGroup.shutdownGracefully();
    }
}
