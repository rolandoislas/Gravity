package com.rolandoislas.gravity.net.server.game;

import com.rolandoislas.gravity.net.common.NetUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

/**
 * @author Rolando.
 */
public class GameServerInitializer {

    public static int port = 48051;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;

    public void run() throws InterruptedException {
        // Groups
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            // Decoders
                            DelimiterBasedFrameDecoder delimiterDecoder = new DelimiterBasedFrameDecoder(150, true,
                                    NetUtil.stringToByteBuf("\n"));
                            GameServerDecoder decoder = new GameServerDecoder();
                            // Handlers
                            GameServerPlayerHandler playerHandler = new GameServerPlayerHandler();
                            GameServerLogicHandler logicHandler = new GameServerLogicHandler();
                            GameServerChatHandler chatHandler = new GameServerChatHandler(playerHandler);
                            // Add to pipeline
                            ch.pipeline().addLast(delimiterDecoder, decoder, playerHandler, logicHandler, chatHandler);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
