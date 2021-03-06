package com.wuwei.nettyServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author wuwei
 * @since  2018/7/19 17:57
 * 监听Socket客户端连接的Netty服务端
 */
@Service
public class NettyServer {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8888;

    /**
     * 用于分配处理业务线程的线程组个数
     */
    protected static final int GROUP_SIZE = Runtime.getRuntime().availableProcessors() * 2; // 默认

    /**
     * 业务处理线程大小
     */
    protected static final int THREAD_SIZE = 4;

    /**
     * NioEventLoopGroup实际上就是个线程池,
     * NioEventLoopGroup在后台启动了n个NioEventLoop来处理Channel事件,
     * 每一个NioEventLoop负责处理m个Channel,
     * NioEventLoopGroup从NioEventLoop数组里挨个取出NioEventLoop来处理Channel
     */
    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(GROUP_SIZE);
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup(THREAD_SIZE);
    private static final Logger logger = Logger.getLogger(NettyServer.class.getName());

    /**
     * SpringBoot项目启动后, 自动启动WebSocket Netty服务端
     */
    @PostConstruct
    private void startupNettyServer() {
        new Thread(this::run).start();
    }

    private void run() {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.handler(new LoggingHandler(LogLevel.INFO));
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    ChannelPipeline pipeline = ch.pipeline();
                    ch.pipeline().addLast("frameDecoder", new LineBasedFrameDecoder(1024));
                    pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                    pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
                    pipeline.addLast("handler", new NettyServerHandler());
                }
            });
            bootstrap.option(ChannelOption.SO_BACKLOG, 128);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = bootstrap.bind(HOST, PORT).sync();
            logger.log(Level.INFO, "Netty服务端已启动，等待客户端连接...");
            future.channel().closeFuture().sync(); //相当于在这里阻塞，直到server channel关闭
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
