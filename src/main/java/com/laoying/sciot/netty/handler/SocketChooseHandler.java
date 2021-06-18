package com.laoying.sciot.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.DefaultChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class SocketChooseHandler extends ByteToMessageDecoder {
    /**
     * WebSocket握手的协议前缀
     */
    private static final String WEBSOCKET_PREFIX = "GET /";

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        String protocol = getBufStart(in);
        System.out.println("------------------protocol----------------" + protocol);
        ChannelPipeline pipeline = ctx.channel().pipeline();
        String delimiter = "##";
        if (protocol.startsWith(WEBSOCKET_PREFIX)) {
            //  websocket连接时，执行以下处理
            // HttpServerCodec：将请求和应答消息解码为HTTP消息
            pipeline.addLast("http-codec", new HttpServerCodec());

            // HttpObjectAggregator：将HTTP消息的多个部分合成一条完整的HTTP消息
            pipeline.addLast("aggregator", new HttpObjectAggregator(65535));

            // ChunkedWriteHandler：向客户端发送HTML5文件,文件过大会将内存撑爆
            pipeline.addLast("http-chunked", new ChunkedWriteHandler());

            pipeline.addLast("WebSocketAggregator", new WebSocketFrameAggregator(65535));

            //  若超过80秒未收到约定心跳，则主动断开channel释放资源
//            pipeline.addLast(new IdleStateHandler(1, 0, 0,TimeUnit.MINUTES));

            //用于处理websocket, /ws为访问websocket时的uri
            pipeline.addLast("ProtocolHandler", new WebSocketServerProtocolHandler("/ws"));
            pipeline.addLast("WebsocketHandler", new NettyWebSocketHandler());
        } else if(protocol.toUpperCase().startsWith("LY")){
            //  常规TCP连接时，执行以下处理
            pipeline.addLast("delimiterBasedFrameDecoder", new DelimiterBasedFrameDecoder(2048, Unpooled.wrappedBuffer(delimiter.getBytes())));
            pipeline.addLast("stringEncoder", new StringEncoder(Charset.forName("GB2312")));
            pipeline.addLast("stringDecoder", new StringDecoder(Charset.forName("GB2312")));
            pipeline.addLast("idleStateHandler", new IdleStateHandler(1, 0, 0, TimeUnit.MINUTES));//心跳检测
            pipeline.addLast("delimiterBasedFrameEncoder", new DelimiterBasedFrameEncoder(delimiter));
            pipeline.addLast("Tcp-Server", new LYServerHandler());
        }
        in.resetReaderIndex();
        pipeline.remove(this.getClass());
    }

    private String getBufStart(ByteBuf in) throws UnsupportedEncodingException {
        int length = in.readableBytes();
        // 标记读位置
        in.markReaderIndex();
        byte[] content = new byte[length];
        in.readBytes(content);
        return new String(content, "utf-8");
    }
   /* @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().pipeline() instanceof DefaultChannelPipeline);
        log.info("-------------channelActive------------" + ctx.channel().remoteAddress());
        System.out.println("--------------------------");
    }*/


   /* @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
//        byte[] array = ctx.alloc().buffer().array();
//        String s = new String(array);
//        System.out.println(s);
        log.info("-----------handlerAdded----------" + ctx.channel().remoteAddress());
    }*/
/*
    @Override
    protected void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
        log.info("---------------handlerRemoved0--------------" + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("-----------channelInactive----------" + ctx.channel().remoteAddress());
    }*/
}
