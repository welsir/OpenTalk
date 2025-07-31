package com.opentalk.netty.message;

import com.opentalk.netty.compress.VarInt;
import lombok.Data;

/**
 * @author welsir
 * @description :
 * @date 2025/7/20
 */
@Data
public class MetaMessageHead {

    private int type;

    private byte[] data;

    public MetaMessageHead(int type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int calculateTotalLength() {
        int dataLength = calculateDataLength();
        return 1 + VarInt.computeVarInt32Size(dataLength)+dataLength;
    }

    public int calculateDataLength(){
        return data==null?0:data.length;
    }

}