package com.common.entity.DTO;

import lombok.Data;

@Data
public class CreateRoomRequestDTO {

    /**
     * 直播标题
     */
    private String name;

    /**
     * 主播ID
     */
    private Long ownerId;
}
