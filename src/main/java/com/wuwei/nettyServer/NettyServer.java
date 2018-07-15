package com.wuwei.nettyServer;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

/**
 * @author wuwei
 * @description
 * @date 2018.07.15 18:40
 */
public class NettyServer {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 9999;
    /**
     * 用于分配处理业务线程的线程组个数
     */
    protected static final int GROUP_SIZE = Runtime.getRuntime().availableProcessors() * 2; // 默认
    /**
     * 业务出现线程大小
     */
    protected static final int THREAD_SIZE = 4;
    /*
     * NioEventLoopGroup实际上就是个线程池,
     * NioEventLoopGroup在后台启动了n个NioEventLoop来处理Channel事件,
     * 每一个NioEventLoop负责处理m个Channel,
     * NioEventLoopGroup从NioEventLoop数组里挨个取出NioEventLoop来处理Channel
     */
    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(GROUP_SIZE);
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup(THREAD_SIZE);
    private static final Logger logger = Logger.getLogger(NettyServer.class.getName());

    private static void run() {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                    pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                    pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                    pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
                    pipeline.addLast(new WebSocketServerHandler());
                }
            });
            bootstrap.bind(HOST, PORT).sync();
//            Channel ch = bootstrap.bind(new InetSocketAddress(HOST, PORT)).sync().channel();
//            ch.closeFuture().sync();
            logger.log(Level.INFO, "Netty服务端已启动...");
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    public static void shutdown() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public static void main(String[] args) {
        NettyServer.run();
    }
}
