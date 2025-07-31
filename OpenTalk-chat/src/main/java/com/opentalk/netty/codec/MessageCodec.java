package com.opentalk.netty.codec;

import com.opentalk.netty.compress.VarInt;
import com.opentalk.netty.message.MetaMessage;
import com.opentalk.netty.message.MetaMessageHead;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author welsir
 * @Date 2024/6/24 10:22
 */
public class MessageCodec {

    private static Logger log = LoggerFactory.getLogger(MessageCodec.class);
    private static final int MAX_BODY_SIZE_TO_COMPRESS = 2048;

    public static void encode(ByteBuf buf, MetaMessage msg) {
        buf.writeByte(msg.getVersion());
        buf.writeBoolean(msg.isHeartBeat());
        if (msg.isHeartBeat()) {
            return;
        }
        buf.writeByte(msg.getCmd());

        List<MetaMessageHead> headers = msg.getHeaders();
        int headerCount = headers == null ? 0 : headers.size();
        int headerLength = 0;
        for (int i = 0; i < headerCount; i++) {
            headerLength += headers.get(i).calculateTotalLength();
        }
        byte[] body = msg.getBody();
        int bodyLength = body == null ? 0 : body.length;
        msg.setLength(headerLength + bodyLength);
        VarInt.writeVarInt32(buf, msg.getLength());

        buf.writeByte(headerCount);
        for (int i = 0; i < headerCount; i++) {
            MetaMessageHead header = headers.get(i);
            VarInt.writeVarInt32(buf, header.calculateDataLength());
            buf.writeByte(header.getType());
            buf.writeBytes(header.getData());
        }

        if (bodyLength != 0) {
            buf.writeBytes(body);
        }

    }

    public static MetaMessage decode(ByteBuf buf) {
        MetaMessage msg = new MetaMessage();
        try {
            msg.setVersion(buf.readByte());
            msg.setHeartBeat(buf.readBoolean());
            if(msg.isHeartBeat()){
                return msg;
            }
            msg.setCmd(buf.readByte());
            msg.setLength(VarInt.readVarInt32(buf));
            int totalLength = msg.getLength();
            List<MetaMessageHead> headers = new ArrayList<>();
            int headerCount = buf.readByte();
            for (int i = 0; i < headerCount; i++) {
                int headerLength = VarInt.readVarInt32(buf);
                int headerType = buf.readByte();
                MetaMessageHead header = new MetaMessageHead(headerType, getFromBuf(buf, headerLength));
                headers.add(header);
                totalLength-=header.calculateTotalLength();
            }
            msg.setHeaders(headers);
            byte[] body = getFromBuf(buf, totalLength);
            msg.setBody(body);
        }catch (Exception e){
            log.error("msg decoder failed:{}",e.getMessage());
            //todo: 抛异常
        }
        return msg;
    }

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    public static byte[] getFromBuf(ByteBuf in, int length) {
        if (length <= 0) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] bTemp = new byte[length];
        in.readBytes(bTemp);
        return bTemp;
    }
}
