package com.opentalk.netty.factory;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.SystemPropertyUtil;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;
import io.netty.channel.socket.ServerSocketChannel;

import java.util.concurrent.ThreadFactory;

public class NettyFactory {

    public static EventLoopGroup eventLoopGroup(int threads, String threadName) {
        ThreadFactory threadFactory = new AffinityThreadFactory(threadName, AffinityStrategies.DIFFERENT_CORE);
        return supportEpoll() ? new EpollEventLoopGroup(threads, threadFactory) : new NioEventLoopGroup(threads, threadFactory);
    }

    private static boolean supportEpoll() {
        return SystemPropertyUtil.get("os.name").toLowerCase().contains("linux") && Epoll.isAvailable();
    }

    public static Class<? extends ServerSocketChannel> serverSocketChannelClass() {
        return supportEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
    }
}