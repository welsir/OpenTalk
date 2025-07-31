package com.opentalk.netty.compress;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;

/**
 * @Description
 * @Author welsir
 * @Date 2024/6/26 14:32
 */
public class VarInt {

    public static int readVarInt32(ByteBuf buffer) {
        if (!buffer.isReadable()) {
            return 0;
        }
        buffer.markReaderIndex();
        int res = 0;
        int shift = 0;
        for(int i=0;i<5;i++){
            if(!buffer.isReadable()){
                buffer.resetReaderIndex();
                return 0;
            }
            byte tmp = buffer.readByte();
            res |= (tmp & 127) << shift;
            if (tmp >= 0) {
                return res;
            }
            shift += 7;
        }
        return 0;
    }

    public static void writeVarInt32(ByteBuf out, int value) {
        while (true) {
            if ((value & ~0x7F) == 0) {
                out.writeByte(value);
                return;
            } else {
                out.writeByte((value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }

    public static int computeVarInt32Size(final int value) {
        int i;
        for(i=1;i<5;i++){
            if((value & (0xffffffff << 7*i)) == 0){
                return i;
            }
        }
        return i;
    }

}
