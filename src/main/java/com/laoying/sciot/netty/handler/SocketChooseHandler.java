package com.laoying.sciot.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class SocketChooseHandler extends ByteToMessageDecoder {
    /**
     * WebSocket握手的协议前缀
     */
    private static final String WEBSOCKET_PREFIX = "GET /";

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        String protocol = getBufStart(in);
        ChannelPipeline pipeline = ctx.channel().pipeline();
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
            pipeline.addLast(new IdleStateHandler(80, 0, 0));
//            ctx.pipeline().addLast(new NettyHeartKeeper(NettyHeartKeeper.TYPE_WEBSOCKET));

            //用于处理websocket, /ws为访问websocket时的uri
            pipeline.addLast("ProtocolHandler", new WebSocketServerProtocolHandler("/ws"));
            pipeline.addLast("WebsocketHandler", new NettyWebSocketHandler());
        } else {
            //  常规TCP连接时，执行以下处理
            pipeline.addLast(new StringEncoder(Charset.forName("GB2312")));
            pipeline.addLast(new StringDecoder(Charset.forName("GB2312")));
            pipeline.addLast(new IdleStateHandler(1, 1, 1, TimeUnit.MINUTES));//心跳检测
            // 当服务器端收到数据后需要使用解码器进行解码。applicationContext.getBean(LYServerHandler.class)
            pipeline.addLast("Tcp-Server", new LYServerHandler());
        }
        in.resetReaderIndex();
        pipeline.remove(this.getClass());
    }

    private String getBufStart(ByteBuf in) {
        int length = in.readableBytes();
        // 标记读位置
        in.markReaderIndex();
        byte[] content = new byte[length];
        in.readBytes(content);
        return new String(content);
    }
}
