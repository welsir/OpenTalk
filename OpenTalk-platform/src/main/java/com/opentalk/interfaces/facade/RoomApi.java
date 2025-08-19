package com.opentalk.interfaces.facade;

import com.opentalk.application.service.RoomApplicationService;
import com.opentalk.common.result.Result;
import com.opentalk.interfaces.dto.CreateRoomRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author welsir
 * @description : 房间API接口
 * @date 2025/7/29
 */
@RestController
@RequestMapping("/openTalk/room")
public class RoomApi {

    @Autowired
    private RoomApplicationService roomApplicationService;

    /**
     * 创建房间
     */
    @PostMapping("/create")
    public Result<?> createRoom(@RequestBody CreateRoomRequest request) {
        return roomApplicationService.createGroupRoom(request);
    }

    /**
     * 加入房间
     */
    @PostMapping("/join")
    public Result<?> joinRoom(@RequestParam String roomId, @RequestParam String userId) {
        return roomApplicationService.joinGroupRoom(roomId, userId);
    }

    /**
     * 退出房间
     */
    @PostMapping("/leave")
    public Result<?> leaveRoom(@RequestParam String roomId, @RequestParam String userId) {
        return roomApplicationService.leaveGroupRoom(roomId, userId);
    }

    /**
     * 获取房间信息
     */
    @GetMapping("/info")
    public Result<?> getRoomInfo(@RequestParam String roomId) {
        return roomApplicationService.getRoomInfo(roomId);
    }

    /**
     * 获取公开房间列表
     */
    @GetMapping("/public")
    public Result<?> getPublicRooms(@RequestParam(defaultValue = "10") int limit) {
        return roomApplicationService.getPublicRooms(limit);
    }

    /**
     * 获取用户创建的房间列表
     */
    @GetMapping("/owned")
    public Result<?> getOwnedRooms(@RequestParam String userId) {
        return roomApplicationService.getOwnedRooms(userId);
    }

    /**
     * 获取用户加入的房间列表
     */
    @GetMapping("/joined")
    public Result<?> getJoinedRooms(@RequestParam String userId) {
        return roomApplicationService.getJoinedRooms(userId);
    }
}