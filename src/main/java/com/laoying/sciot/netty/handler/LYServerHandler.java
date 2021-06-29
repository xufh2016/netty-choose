package com.laoying.sciot.netty.handler;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.laoying.sciot.netty.config.NettyConfig;
import com.laoying.sciot.netty.service.PushService;
import com.laoying.sciot.util.CRC16Check;
import io.netty.channel.*;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * 1、数据不完整：INCOMPLETE_DATA
 * 2、数据错误：ERROR_DATA
 */
@ChannelHandler.Sharable
public class LYServerHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LYServerHandler.class);

    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    //读事件
    public void channelRead0(ChannelHandlerContext ctx, String rcvMsg) throws UnsupportedEncodingException {
        Channel channel = ctx.channel();
        //获取客户端地址
        SocketAddress socketAddress = channel.remoteAddress();
        LOGGER.info("接收到来自客户端：" + socketAddress.toString() + "的数据是：" + rcvMsg);
        if (rcvMsg != null && !"".equals(rcvMsg.trim())) {
            if (!rcvMsg.contains("{")||!rcvMsg.contains("}")){
                channel.writeAndFlush("ERROR_DATA");
                return;
            }
            String jsonStr = rcvMsg.substring(rcvMsg.indexOf("{"), rcvMsg.lastIndexOf("}") + 1);
            String rcvCrc = rcvMsg.substring(rcvMsg.lastIndexOf("}") + 1);

            String crc = CRC16Check.getCrc(jsonStr.getBytes());
            //crc校验值相等，则进行存库
            if (rcvCrc.equalsIgnoreCase(crc)) {
                //存库操作，并推送到前端(1、检测烟气的数据；2、设备在线还是不在线的状态)
//                LOGGER.info("-------开始接受数据----------");
                JSONObject jsonObject = JSONUtil.parseObj(jsonStr);

                String instID = jsonObject.get("InstID").toString();

//                LOGGER.info("设备编号是："+instID);
//                ChannelGroup channelGroup = NettyConfig.getChannelGroup();
//                channelGroup.add(channel);
//                ConcurrentHashMap<String, Channel> userChannelMap = NettyConfig.getUserChannelMap();
//                userChannelMap.put(instID,channel);

                //2、推送数据
                new PushService().pushMsgToAll(jsonStr);
            }

//            LOGGER.info("接收到的crc码是：" + rcvCrc);
//            LOGGER.info("jsonStr是：" + jsonStr);
//            LOGGER.info("jsonStr的crc校验值是：" + CRC16Check.getCrc(jsonStr.getBytes()));
        }
//        LOGGER.info("执行channelRead~~~~~~~~~~~~");
//
//        ctx.channel().writeAndFlush("-----------server says hi---------------------" + ctx.channel().remoteAddress());
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
//                  发生读事件空闲，则告诉数据库及客户端设备已离线
                    ctx.writeAndFlush("heartbeat").addListener(ChannelFutureListener.CLOSE_ON_FAILURE) ;
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
        LOGGER.info("-----------exceptionCaught----------" + cause.getMessage());
        //异常发生时关闭连接
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //tcp连接断开时触发。websocket通知前端设备已离线
        LOGGER.info("--------------handlerRemoved------------");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        LOGGER.info("-------TCP Socket连接已添加-------");
        LOGGER.info("---------------handlerAdded------------");
    }
}
