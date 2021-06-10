package com.laoying.sciot.netty.handler;

import com.laoying.sciot.util.CRC16Check;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.SocketAddress;

@ChannelHandler.Sharable
public class LYServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LYServerHandler.class);

    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    //读事件
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException {
        Channel channel = ctx.channel();
        //获取客户端地址
        SocketAddress socketAddress = channel.remoteAddress();
        String rcvMsg = (String) msg;
        if (rcvMsg != null && !"".equals(rcvMsg.trim())) {
            String jsonStr = rcvMsg.substring(rcvMsg.indexOf("{"), rcvMsg.lastIndexOf("}") + 1);
            String rcvCrc = rcvMsg.substring(rcvMsg.lastIndexOf("}") + 1);

            String crc = CRC16Check.getCrc(jsonStr.getBytes());
            //crc校验值相等，则进行存库
            if (rcvCrc.equalsIgnoreCase(crc)) {
                //存库操作，并推送到前端
                LOGGER.info("-------开始接受数据----------");
            }
            LOGGER.info("---------------Server接收到来自客户端的消息-------------" + rcvMsg);

            LOGGER.info("接收到的crc码是：" + rcvCrc);
            LOGGER.info("jsonStr是：" + jsonStr);
            LOGGER.info("jsonStr的crc校验值是：" + CRC16Check.getCrc(jsonStr.getBytes()));
        }
        LOGGER.info("执行channelRead~~~~~~~~~~~~");


        channelGroup.forEach(item -> {
            if (item != channel) {
                item.writeAndFlush(item.remoteAddress() + "----send msg---" + rcvMsg);
            } else {
                item.writeAndFlush("[self]----" + rcvMsg);
            }
        });

        ctx.channel().writeAndFlush("-----------server says hi---------------------" + ctx.channel().remoteAddress());
//                                            ByteBuf byteBuf = Unpooled.copiedBuffer("Hello , This is Server!!!", CharsetUtil.UTF_8);
//                                            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf);
//                                            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
//                                            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, byteBuf.readableBytes());
//                                            ctx.writeAndFlush(response);
    }


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("------------------channelRegistered---------");
    }


    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("-----------channelUnregistered----------");
    }

    @Override
    //该方法表示通道已连接，连接好之后会自动回调
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channelGroup.writeAndFlush("---------------------[Server]-" + channel.remoteAddress() + "----------join in----------");

        channelGroup.add(channel);
        LOGGER.info("--------------channelActive------------");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        LOGGER.info("----------channelInactive--------");
        channelGroup.writeAndFlush("[Server]-" + channel.remoteAddress() + "-----left----");
        channelGroup.remove(channel);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("---------------channelReadComplete--------------");
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("------------channelWritabilityChanged---------");
    }

    @Override
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
                    break;
                case WRITER_IDLE:
                    eventType = "WRITER_IDLE";
                    break;
                default:
                    break;
            }
            ctx.channel().close();
        }
        LOGGER.info("---------------userEventTriggered--------------");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.info("-----------exceptionCaught----------");
        //异常发生时关闭连接
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("--------------handlerRemoved------------");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("---------------handlerAdded------------");
    }
}
