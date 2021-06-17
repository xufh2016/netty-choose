package com.laoying.sciot.netty.handler;

import com.laoying.sciot.netty.config.NettyConfig;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

/**
 * websocket处理器
 */
@Slf4j
public class NettyWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        //  在此处接收客户端发送的信息
        Channel channel = ctx.channel();

        log.info("来自webSocket客户端" + channel.remoteAddress() + "的信息: " + msg.text());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("handlerAdded被调用" + ctx.channel().id().asLongText());
        NettyConfig.getChannelGroup().add(ctx.channel());
        log.info("添加webSocket连接");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("移除webSocket连接，ChannelId：" + ctx.channel().id().asLongText());
        NettyConfig.getChannelGroup().remove(ctx.channel());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("webSocket建立连接");

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("webSocket断开连接");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("异常：{}", cause.getMessage());
        // 删除通道
        NettyConfig.getChannelGroup().remove(ctx.channel());
        ctx.close();
    }

   /* @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            String eventType = null;
            switch (event.state()) {
                case ALL_IDLE:
                    eventType = "ALL_IDLE";
                    break;
                case READER_IDLE:
                    eventType = "READER_IDLE";
//                  发生读事件空闲，则告诉数据库及客户端设备已离线
                    ctx.writeAndFlush("heartbeat").addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                    break;
                case WRITER_IDLE:
                    eventType = "WRITER_IDLE";
                    break;
                default:
                    break;
            }
            ctx.channel().close();
        }
    }*/
}
