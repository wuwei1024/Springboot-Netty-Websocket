package com.wuwei.webSocketServer;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author wuwei
 * @since 2018.07.15 18:40
 * 监听WebSocket连接的Netty服务端
 */
@Service
public class NettyServer {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 9999;
    private static final String WEB_SOCKET_PATH = "/websocket";

    /**
     * 用于分配处理业务线程的线程组个数
     */
    private static final int GROUP_SIZE = Runtime.getRuntime().availableProcessors() * 2; // 默认

    /**
     * 业务处理线程大小
     */
    private static final int THREAD_SIZE = 4;

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
    public void startupWebSocketServer() {
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
                    //websocket协议本身是基于http协议的，所以这边也要使用http解编码器
                    pipeline.addLast("httpCodec", new HttpServerCodec());
                    //以块的方式来写的处理器
                    pipeline.addLast("httpChunked", new ChunkedWriteHandler());
                    //netty是基于分段请求的，HttpObjectAggregator的作用是将请求分段再聚合,参数是聚合字节的最大长度
                    pipeline.addLast("aggregator", new HttpObjectAggregator(64 * 1024));
                    pipeline.addLast("webSocketPath", new WebSocketServerProtocolHandler(WEB_SOCKET_PATH));
                    pipeline.addLast("webSocketHandler", new NettyServerHandler());
                }
            });
            ChannelFuture future = bootstrap.bind(HOST, PORT).sync();
            logger.log(Level.INFO, "WebSocket Netty服务端已启动，等待客户端连接...");
            future.channel().closeFuture().sync(); //相当于在这里阻塞，直到server channel关闭
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
