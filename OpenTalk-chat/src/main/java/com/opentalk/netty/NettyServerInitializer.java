package com.opentalk.netty;

import com.opentalk.netty.codec.ServerMessageWebSocketDecoder;
import com.opentalk.netty.codec.ServerMessageWebSocketEncoder;
import com.opentalk.netty.handler.NettyCenterHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @Description
 * @Author welsir
 * @Date 2024/6/23 19:31
 */
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    //Netty事件统一处理器
    private static final NettyCenterHandler NETTY_CENTER_HANDLER = new NettyCenterHandler();

    private static final ServerMessageWebSocketEncoder WEBSOCKET_ENCODER = new ServerMessageWebSocketEncoder();

    private static final ServerMessageWebSocketDecoder WEBSOCKET_DECODER = new ServerMessageWebSocketDecoder();

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("httpServerCodec", new HttpServerCodec());
        pipeline.addLast("httpObjectAggregator", new HttpObjectAggregator(2048));
        pipeline.addLast("webSocketServerProtocolHandler", new WebSocketServerProtocolHandler("/chat"));
        pipeline.addLast("chunkedWriteHandler",new ChunkedWriteHandler());
        pipeline.addLast("encoder", WEBSOCKET_ENCODER);
        pipeline.addLast("decoder", WEBSOCKET_DECODER);
        pipeline.addLast("handler", NETTY_CENTER_HANDLER);
    }
}
