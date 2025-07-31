package com.opentalk.domain.room.factory;

import com.opentalk.domain.room.entity.GroupRoom;
import com.opentalk.domain.room.repository.po.RoomPO;
import org.springframework.stereotype.Service;

/**
 * @author welsir
 * @description :
 * @date 2025/7/29
 */
@Service
public class RoomFactory {

    public GroupRoom convertGroupRoom(RoomPO roomPO) {

        GroupRoom room = new GroupRoom();

        return room;
    }

}