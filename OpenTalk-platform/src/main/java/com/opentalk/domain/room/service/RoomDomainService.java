package com.opentalk.domain.room.service;

import com.opentalk.common.entity.req.CreateRoomRequest;
import com.opentalk.common.entity.resp.CreateRoomResponse;
import com.opentalk.domain.room.entity.GroupRoom;
import com.opentalk.domain.room.factory.RoomFactory;
import com.opentalk.domain.room.repository.facade.RoomRepositoryInterface;
import com.opentalk.domain.room.repository.po.RoomPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author welsir
 * @description :
 * @date 2025/7/29
 */
@Service
@Slf4j
public class RoomDomainService {

    @Resource
    RoomRepositoryInterface roomRepository;
    @Resource
    RoomFactory roomFactory;

    public CreateRoomResponse createRoom(CreateRoomRequest request) {

        return null;

    }

    public GroupRoom findRoomByRoomId(String roomId) {
        RoomPO po = roomRepository.findById(roomId);
        if(po == null) {
            throw new RuntimeException("");
        }
        GroupRoom room = roomFactory.convertGroupRoom(po);
        return room;
    }

    public void removeRoomMember(String uid){

    }
}