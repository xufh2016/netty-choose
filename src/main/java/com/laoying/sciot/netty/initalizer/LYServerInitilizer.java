package com.laoying.sciot.netty.initalizer;

import com.laoying.sciot.netty.handler.SocketChooseHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class LYServerInitilizer extends ChannelInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(LYServerInitilizer.class);

    @Override
    protected void initChannel(Channel ch) throws Exception {
//        String delimiter = "##";
        ChannelPipeline pipeline = ch.pipeline();
//                                    pipeline.addLast(new HttpServerCodec());
        //6、添加具体handler,注意一点，addLast中的对象不要搞成单例，需要多实例
//                                    pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
//                                    pipeline.addLast(new LengthFieldPrepender(4));
//        pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));//StringDecoder将ByteBuf转换为字符串,解码器，客户端向服务器端传输数据时需要编码器，
//        pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
//        pipeline.addLast(new LineBasedFrameDecoder(2048));
//        pipeline.addLast(new LengthFieldBasedFrameDecoder());
        //如果用特殊字符串作为分隔符使用DelimiterBasedFrameDecoder作为解码，如果使用回车换行则使用LineBasedFrameDecoder作为解码器
//        pipeline.addLast("delimiterBasedFrameDecoder",new DelimiterBasedFrameDecoder(2048, Unpooled.wrappedBuffer(delimiter.getBytes())));
//        pipeline.addLast("stringEncoder",new StringEncoder(Charset.forName("GB2312")));
//        pipeline.addLast("stringDecoder",new StringDecoder(Charset.forName("GB2312")));
//        pipeline.addLast("idleStateHandler",new IdleStateHandler(1, 0, 0, TimeUnit.MINUTES));//心跳检测
//        pipeline.addLast("delimiterBasedFrameEncoder",new DelimiterBasedFrameEncoder(delimiter));
        pipeline.addLast("socketChoose",new SocketChooseHandler());
        // 当服务器端收到数据后需要使用解码器进行解码。applicationContext.getBean(LYServerHandler.class)
//        pipeline.addLast("Tcp-Server", new LYServerHandler());
    }
}
