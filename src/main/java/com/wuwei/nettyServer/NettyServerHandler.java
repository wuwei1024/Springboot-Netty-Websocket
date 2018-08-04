package com.wuwei.nettyServer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wuwei
 * @since 2018/7/19 18:02
 * 接收客户端消息，并进行响应处理
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<String> {

    /**
     * 缓存所有的Netty客户端连接
     */
    private static final ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        Channel channel = ctx.channel();
        String remoteAddress = channel.remoteAddress().toString();
        channels.put(remoteAddress, channel);
        String sendTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String serverMsg = sendTime + "(" + remoteAddress + "): " + msg;
        for (Channel c : channels.values()) {
            //转发消息给其它的客户端
            if (c != channel) c.writeAndFlush(serverMsg + "\r\n");
        }
    }
}
