package com.opentalk.common.entity.resp;

import lombok.Data;

/**
 * @author welsir
 * @description :
 * @date 2025/7/29
 */
@Data
public class CreateRoomResponse {

    private String roomId;
    private String roomName;
    private String Type;

}