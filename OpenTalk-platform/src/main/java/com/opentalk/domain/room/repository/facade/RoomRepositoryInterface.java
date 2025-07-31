package com.opentalk.domain.room.repository.facade;

import com.opentalk.domain.room.repository.po.RoomPO;

/**
 * @author welsir
 * @description :
 * @date 2025/7/29
 */
public interface RoomRepositoryInterface {

    void createRoom(String roomName);

    RoomPO findById(String roomId);

}