package com.wuwei.nettyClient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author wuwei
 * @description
 * @date 2018.07.15 19:44
 */
public class NettyClient {

    private static String HOST = "127.0.0.1";
    private static int PORT = 9999;

    private static Bootstrap bootstrap = getBootstrap();
    private static Channel channel = getChannel(HOST, PORT);
    private static Logger logger = Logger.getLogger(NettyClient.class.getName());

    /**
     * 初始化Bootstrap
     */
    private static Bootstrap getBootstrap() {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
                pipeline.addLast("handler", new NettyClientHandler());
            }
        });
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        return bootstrap;
    }

    private static Channel getChannel(String host, int port) {
        Channel channel = null;
        try {
            channel = bootstrap.connect(host, port).sync().channel();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "连接Server失败！", e);
            return null;
        }
        return channel;
    }

    public static void sendMsgToNettyServer(String msg) throws Exception {
        if (channel != null) {
            channel.writeAndFlush(msg).sync();
        } else {
            logger.log(Level.SEVERE, "消息发送失败,连接尚未建立!");
        }
    }

    public static void main(String[] args) throws Exception {
        NettyClient.sendMsgToNettyServer("Hello Server");
    }
}
