package com.laoying.sciot.netty.bootstrap;

import com.laoying.sciot.netty.initalizer.LYServerInitilizer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * 与硬件通过tcp连接的server
 */
@Component
public class NettyBootstrapRunner implements ApplicationRunner, ApplicationListener<ContextClosedEvent>, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyBootstrapRunner.class);

    @Value("${netty.socket.port}")
    private int port;
    private ApplicationContext applicationContext;

    private Channel serverChannel;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        //1、启动器，负责组装netty组件，启动服务器
        try {
            ChannelFuture channelFuture = new ServerBootstrap()
                    //2、BossEventLoop、WorkEventLoop（selector，thread），group组
                    .group(bossGroup, workerGroup)
                    //3、选择服务器的ServerSocketChannel实现
                    .channel(NioServerSocketChannel.class)
                    //4、boss负责处理连接，worker（child）负责读写，childHandler决定了将来child能执行哪些操作（handler）
                    .childHandler(
                            //5、代表和客户端进行数据读写的通道Initializer初始化，负责添加别的handlerapplicationContext.getBean(LYServerInitilizer.class)
                            new LYServerInitilizer()).bind(port).sync();//绑定监听端口
            Channel channel = channelFuture.channel();
            this.serverChannel = channel;
            LOGGER.info("Socket 服务启动，port={}",this.port);
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        if (this.serverChannel!=null) {
            this.serverChannel.close();
        }
        LOGGER.info("Socket服务已停止");
    }

    public  <T> T getBean(Class<T> beanClass){
        return applicationContext.getBean(beanClass);
    }
}
