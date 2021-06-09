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
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("socketChoose",new SocketChooseHandler());
    }
}
