package com.opentalk.netty.codec;

import com.opentalk.netty.message.MetaMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.List;

/**
 * @Description
 * @Author welsir
 * @Date 2024/6/24 9:59
 */
@ChannelHandler.Sharable
public class ServerMessageWebSocketDecoder extends MessageToMessageDecoder<WebSocketFrame> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, WebSocketFrame msg, List<Object> list) throws Exception {
        if (msg instanceof BinaryWebSocketFrame) {
            ByteBuf buf = msg.content();
            if (buf == null) {
                return;
            }
            MetaMessage message = MessageCodec.decode(buf);
            list.add(message);
        } else {
            throw new RuntimeException("unsupported frame type: " + msg.getClass().getName());
        }
    }
}
