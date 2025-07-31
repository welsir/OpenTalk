package com.opentalk.netty.config;

import lombok.Data;

/**
 * @author welsir
 * @description :
 * @date 2025/7/20
 */
@Data
public class NettyServerConfig {

    private int port = 9989;
    private int maxQueueSize = 1024;

}