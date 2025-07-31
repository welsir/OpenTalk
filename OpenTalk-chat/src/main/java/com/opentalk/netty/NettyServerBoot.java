package com.opentalk.netty;

import com.opentalk.netty.config.NettyServerConfig;
import com.opentalk.netty.factory.NettyFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class NettyServerBoot implements ApplicationListener<ContextClosedEvent>, CommandLineRunner {

    @Resource
    NettyServerConfig properties;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channelWs;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("Shutdown Netty Server...");
        try {
            channelWs.close();
        }catch (Throwable e){
            log.error("websocket channel close failed !",e);
        }
    }

    @Override
    public void run(String... args) {
        ChannelFuture cfWs;
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bossGroup = NettyFactory.eventLoopGroup(1, "bossLoopGroup");
            workerGroup = NettyFactory.eventLoopGroup(4, "workerLoopGroup");
            bootstrap.group(bossGroup,workerGroup)
                    .channel(NettyFactory.serverSocketChannelClass())
                    .option(ChannelOption.SO_BACKLOG,properties.getMaxQueueSize())
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .option(ChannelOption.SO_REUSEADDR,true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,new WriteBufferWaterMark(64*1024,128 * 1024))
                    .childHandler(new NettyServerInitializer());
            cfWs = bootstrap.bind(properties.getPort()).sync();
            channelWs = cfWs.channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}