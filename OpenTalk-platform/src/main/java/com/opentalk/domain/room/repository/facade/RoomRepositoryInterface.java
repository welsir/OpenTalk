package com.opentalk.domain.room.repository.facade;

import com.opentalk.domain.room.entity.GroupRoom;
import com.opentalk.domain.room.repository.po.GroupRoomPO;

/**
 * @author welsir
 * @description :
 * @date 2025/7/29
 */
public interface RoomRepositoryInterface {

    GroupRoomPO findById(String roomId);

    void save(GroupRoom groupRoom);
}