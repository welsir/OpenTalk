package com.opentalk.common.entity.req;

import lombok.Data;

/**
 * @author welsir
 * @description :
 * @date 2025/7/29
 */
@Data
public class CreateRoomRequest {

    private String roomName;
    private String createUid;
}