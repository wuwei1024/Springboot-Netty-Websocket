package com.wuwei.nettyClient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author wuwei
 * @since 2018.07.15 19:44
 * Netty客户端
 */
public class NettyClient {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8888;

    private static final Logger logger = Logger.getLogger(NettyClient.class.getName());
    private static final Bootstrap bootstrap = new Bootstrap();
    private static final EventLoopGroup group = new NioEventLoopGroup();

    static {
        bootstrap.group(group).channel(NioSocketChannel.class);
    }

    /**
     * 获取连接服务端的channel
     */
    private static Channel getChannel() throws InterruptedException {
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                ch.pipeline().addLast("frameDecoder", new LineBasedFrameDecoder(1024));
                pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
                pipeline.addLast("handler", new NettyClientHandler());
            }
        });
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        return bootstrap.connect(HOST, PORT).sync().channel();
    }

    public static void main(String[] args) {
        try {
            Channel channel = getChannel();
            if (channel != null) {
                //行分隔符\r\n，回车\r(return),换行\n(new line)
                //不同的系统有差异：windows:\r\n; mac:\r; linux:\n
                //System.getProperty("line.separator")得到系统的行分隔符，可以屏蔽系统差异
                channel.writeAndFlush("Hello Server" + "\r\n");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }
}
