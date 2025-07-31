package com.opentalk.application.service;

import com.opentalk.common.entity.req.CreateRoomRequest;
import com.opentalk.common.result.Result;
import com.opentalk.common.result.ResultUtils;
import com.opentalk.domain.room.entity.GroupRoom;
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


    public Result<?> create(CreateRoomRequest createRoomRequest) {
        boolean status = userDomainService.checkUserValid(createRoomRequest.getCreateUid());
        if(status){
            return ResultUtils.success(roomDomainService.createRoom(createRoomRequest));
        }else{
            throw new RuntimeException("error");
        }
    }

    public Result<?> quitGroupRoom(String roomId,String uid) {

        boolean status = userDomainService.checkUserValid(uid);
        if(status){
            GroupRoom room = roomDomainService.findRoomByRoomId(roomId);
        }

        return ResultUtils.success();
    }
}