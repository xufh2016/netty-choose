package com.laoying.sciot.netty.handler;

import com.laoying.sciot.netty.bootstrap.NettyBootstrapRunner;
import com.laoying.sciot.netty.config.PipelineAdd;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import javax.annotation.Resource;
import java.util.List;

public class SocketChooseHandler extends ByteToMessageDecoder {
    private static final int MAX_LENGTH = 23;
    private static final String WEBSOCKET_PREFIX="GET /";
    @Resource
    private NettyBootstrapRunner nettyBootstrapRunner;
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        String protocol = getBufStart(in);
        if (protocol.startsWith(WEBSOCKET_PREFIX)) {
            nettyBootstrapRunner.getBean(PipelineAdd.class).websocketAdd(ctx);

            //对于 webSocket ，不设置超时断开
            ctx.pipeline().remove(IdleStateHandler.class);
            ctx.pipeline().remove(LengthFieldBasedFrameDecoder.class);
        }
        in.resetReaderIndex();
        ctx.pipeline().remove(this.getClass());

    }
    private String getBufStart(ByteBuf in){
        int length = in.readableBytes();
        if (length > MAX_LENGTH) {
            length = MAX_LENGTH;
        }

        // 标记读位置
        in.markReaderIndex();
        byte[] content = new byte[length];
        in.readBytes(content);
        return new String(content);
    }

}
