package com.opentalk.common.entity.resp;

import lombok.Builder;
import lombok.Data;

/**
 * @author welsir
 * @description :
 * @date 2025/7/29
 */
@Data
@Builder
public class CreateRoomResponse {

    private String roomId;
    private String roomName;
    private String Type;

}