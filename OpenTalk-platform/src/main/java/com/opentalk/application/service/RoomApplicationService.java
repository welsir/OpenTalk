package com.opentalk.application.service;

import com.opentalk.common.entity.req.CreateRoomRequest;
import com.opentalk.common.result.Result;
import com.opentalk.common.result.ResultUtils;
import com.opentalk.domain.room.entity.GroupRoom;
import com.opentalk.domain.room.factory.RoomFactory;
import com.opentalk.domain.room.repository.facade.RoomRepositoryInterface;
import com.opentalk.domain.room.service.RoomDomainService;
import com.opentalk.domain.user.service.UserDomainService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author welsir
 * @description :
 * @date 2025/7/15
 */
@Service
public class RoomApplicationService {

    @Resource
    UserDomainService userDomainService;
    @Resource
    RoomDomainService roomDomainService;
    @Resource
    private RoomFactory roomFactory;
    @Resource
    private RoomRepositoryInterface roomRepository;

    public Result<?> createGroupRoom(CreateRoomRequest createRoomRequest) {
        try {
            GroupRoom room = roomDomainService.createRoom(createRoomRequest);
            roomRepository.save(room);
            return ResultUtils.success(room);
        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("");
        }
    }

    public Result<?> leaveGroupRoom(String roomId,String uid) {

        boolean status = userDomainService.checkUserValid(uid);
        if(status){
            GroupRoom room = roomDomainService.findRoomByRoomId(roomId);
        }

        return ResultUtils.success();
    }

    public Result<?> joinGroupRoom(String roomId,String uid) {
        boolean flag = userDomainService.checkUserValid(uid);
        if(flag){
            roomDomainService.joinGroupRoom(roomId,uid);
            return ResultUtils.success();
        }else{
            throw new RuntimeException("user status is illegal");
        }
    }
}