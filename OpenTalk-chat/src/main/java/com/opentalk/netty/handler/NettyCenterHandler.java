package com.opentalk.netty.handler;

import com.opentalk.netty.util.NetUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.NetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @Description
 * @Author welsir
 * @Date 2024/6/24 10:11
 */
//复用管道处理器，做统一处理
@io.netty.channel.ChannelHandler.Sharable
@Slf4j
public class NettyCenterHandler extends ChannelDuplexHandler {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 握手超时事件
        if (evt instanceof WebSocketServerProtocolHandler.ServerHandshakeStateEvent) {
            WebSocketServerProtocolHandler.ServerHandshakeStateEvent event = (WebSocketServerProtocolHandler.ServerHandshakeStateEvent) evt;
            if (event == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_TIMEOUT) {
                log.warn("The client({}) handshake was timed out, the channel is about to close.",
                        NetUtil.toSocketAddressString((InetSocketAddress) ctx.channel().remoteAddress()));
                ctx.close();
            }
        }
        // 握手完成事件
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            WebSocketServerProtocolHandler.HandshakeComplete event = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            InetSocketAddress remoteAddr = NetUtils.getWsRemoteAddrFromHeader(event.requestHeaders(), ctx.channel());
            log.info("The client({}) handshake was completed successfully and the channel was upgraded to websockets.",
                    NetUtil.toSocketAddressString((InetSocketAddress)ctx.channel().remoteAddress()));
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        super.channelRead(ctx, msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        super.channelActive(ctx);
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

    }

}
