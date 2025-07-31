package com.opentalk.netty.message;

import lombok.Data;

import java.util.List;

/**
 * @author welsir
 * @description :
 * @date 2025/7/20
 */
@Data
public class MetaMessage {

    private int version;
    private boolean heartBeat;
    private int cmd;
    private int length;
    private byte[] body;
    private List<MetaMessageHead> headers;
}