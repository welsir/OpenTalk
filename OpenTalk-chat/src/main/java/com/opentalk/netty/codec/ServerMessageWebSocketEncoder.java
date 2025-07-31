package com.opentalk.netty.codec;

import com.opentalk.netty.message.MetaMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.List;

/**
 * @Description
 * @Author welsir
 * @Date 2024/6/24 9:59
 */
@ChannelHandler.Sharable
public class ServerMessageWebSocketEncoder extends MessageToMessageEncoder<MetaMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, MetaMessage msg, List<Object> list) {
        ByteBuf buf = null;
        try {
            buf = ctx.alloc().ioBuffer();
            MessageCodec.encode(buf, msg);
            WebSocketFrame frame = new BinaryWebSocketFrame(buf);
            list.add(frame);
            buf = null;
        } finally {
            if (buf != null) {
                buf.release();
            }
        }
    }
}
