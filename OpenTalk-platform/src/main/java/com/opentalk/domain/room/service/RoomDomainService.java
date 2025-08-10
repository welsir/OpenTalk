package com.opentalk.domain.room.service;

import com.opentalk.common.entity.req.CreateRoomRequest;
import com.opentalk.domain.room.entity.GroupRoom;
import com.opentalk.domain.room.factory.RoomFactory;
import com.opentalk.domain.room.repository.facade.RoomRepositoryInterface;
import com.opentalk.domain.room.repository.po.GroupRoomPO;
import com.opentalk.domain.user.entity.User;
import com.opentalk.domain.user.entity.po.UserPO;
import com.opentalk.domain.user.service.UserDomainService;
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
    @Resource
    UserDomainService userDomainService;

    public GroupRoom createRoom(CreateRoomRequest request) {
        boolean status = userDomainService.checkUserValid(request.getOwnerUid());
        if(status){
            return roomFactory.createGroupRoom(request.getRoomName(),request.getOwnerUid(),request.getOwnerName());
        }else{
            throw new RuntimeException("error");
        }
    }

    public GroupRoom findRoomByRoomId(String roomId) {
        GroupRoomPO po = roomRepository.findById(roomId);
        if(po == null) {
            throw new RuntimeException("");
        }
        GroupRoom room = roomFactory.convertGroupRoom(po);
        return room;
    }

    public void removeRoomMember(String uid, String roomId) {
        GroupRoomPO po = roomRepository.findById(roomId);
        if(po == null) {
            throw new RuntimeException("room is not exist");
        }

        GroupRoom room = roomFactory.convertGroupRoom(po);
        room.removeMember(uid);
    }

    public void joinGroupRoom(String uid, String roomId) {
        GroupRoomPO po = roomRepository.findById(roomId);
        if(po == null) {
            throw new RuntimeException("room is not exist");
        }

        GroupRoom room = roomFactory.convertGroupRoom(po);
        User user = userDomainService.findById(uid);
        room.addMember(uid,user.getNickname());
    }

}