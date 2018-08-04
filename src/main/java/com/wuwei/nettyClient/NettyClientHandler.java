package com.wuwei.nettyClient;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author wuwei
 * @since 2018.07.15 19:51
 * 接收客户端消息的Netty服务端
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<String> {

    private static Logger logger = Logger.getLogger(NettyClientHandler.class.getName());

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        logger.log(Level.INFO, "client接收到服务器返回的消息:" + msg);
    }
}
