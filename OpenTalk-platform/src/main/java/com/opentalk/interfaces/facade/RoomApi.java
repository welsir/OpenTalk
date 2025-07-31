package com.opentalk.interfaces.facade;

import com.opentalk.common.entity.req.CreateRoomRequest;
import com.opentalk.common.result.Result;
import com.opentalk.application.service.RoomApplicationService;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

/**
 * @author welsir
 * @description :
 * @date 2025/7/29
 */

@RequestMapping("/OpenTalk/room")
public class RoomApi {

    @Resource
    RoomApplicationService roomApplicationService;

    @RequestMapping("/create")
    public Result<?> createRoom(CreateRoomRequest createRoomRequest) {
        try {
            return roomApplicationService.create(createRoomRequest);
        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @RequestMapping("/quit")
    public Result<?> quitRoom(String roomId) {
        return null;
    }
}