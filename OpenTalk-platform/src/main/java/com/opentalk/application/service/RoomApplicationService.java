package com.opentalk.application.service;

import com.opentalk.common.result.Result;
import com.opentalk.common.result.ResultUtils;
import com.opentalk.domain.room.entity.GroupRoom;
import com.opentalk.domain.room.factory.RoomFactory;
import com.opentalk.domain.room.repository.facade.RoomRepositoryInterface;
import com.opentalk.domain.room.service.RoomDomainService;
import com.opentalk.domain.user.service.UserDomainService;
import com.opentalk.interfaces.dto.CreateRoomRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
            // 转换DTO为领域层请求对象
            com.opentalk.common.entity.req.CreateRoomRequest domainRequest = new com.opentalk.common.entity.req.CreateRoomRequest();
            domainRequest.setRoomName(createRoomRequest.getRoomName());
            domainRequest.setOwnerUid(createRoomRequest.getOwnerUid());
            domainRequest.setOwnerName(createRoomRequest.getOwnerName());
            domainRequest.setDescription(createRoomRequest.getDescription());
            domainRequest.setPublic(createRoomRequest.isPublic());
            domainRequest.setMaxMembers(createRoomRequest.getMaxMembers());
            
            GroupRoom room = roomDomainService.createRoom(domainRequest);
            return ResultUtils.success(room);
        } catch (Exception e) {
            return ResultUtils.error("创建房间失败: " + e.getMessage());
        }
    }

    public Result<?> leaveGroupRoom(String roomId, String uid) {
        try {
            // 参数校验
            if (!StringUtils.hasText(roomId) || !StringUtils.hasText(uid)) {
                return ResultUtils.error("房间ID和用户ID不能为空");
            }
            
            // 检查用户是否有效
            boolean status = userDomainService.checkUserValid(uid);
            if (!status) {
                return ResultUtils.error("用户不存在或无效");
            }
            
            // 退出房间
            roomRepository.removeMember(roomId, uid);
            
            return ResultUtils.success("退出房间成功");
        } catch (Exception e) {
            return ResultUtils.error("退出房间失败：" + e.getMessage());
        }
    }

    public Result<?> joinGroupRoom(String roomId, String uid) {
        try {
            // 参数校验
            if (!StringUtils.hasText(roomId) || !StringUtils.hasText(uid)) {
                return ResultUtils.error("房间ID和用户ID不能为空");
            }
            
            // 检查用户是否有效
            boolean flag = userDomainService.checkUserValid(uid);
            if (!flag) {
                return ResultUtils.error("用户不存在或无效");
            }
            
            // 加入房间
            roomDomainService.joinGroupRoom(roomId, uid);
            
            return ResultUtils.success("加入房间成功");
        } catch (Exception e) {
            return ResultUtils.error("加入房间失败：" + e.getMessage());
        }
    }

    /**
     * 获取房间信息
     */
    public Result<?> getRoomInfo(String roomId) {
        try {
            if (!StringUtils.hasText(roomId)) {
                return ResultUtils.error("房间ID不能为空");
            }
            
            GroupRoom room = roomDomainService.findRoomByRoomId(roomId);
            return ResultUtils.success(room);
        } catch (Exception e) {
            return ResultUtils.error("获取房间信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取公开房间列表
     */
    public Result<?> getPublicRooms(int limit) {
        try {
            if (limit <= 0 || limit > 100) {
                limit = 10; // 默认限制
            }
            
            return ResultUtils.success(roomRepository.findPublicRooms(limit));
        } catch (Exception e) {
            return ResultUtils.error("获取公开房间列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户创建的房间列表
     */
    public Result<?> getOwnedRooms(String userId) {
        try {
            if (!StringUtils.hasText(userId)) {
                return ResultUtils.error("用户ID不能为空");
            }
            
            if (!userDomainService.checkUserValid(userId)) {
                return ResultUtils.error("用户不存在或无效");
            }
            
            return ResultUtils.success(roomDomainService.findRoomsByOwner(userId));
        } catch (Exception e) {
            return ResultUtils.error("获取用户房间列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户加入的房间列表
     */
    public Result<?> getJoinedRooms(String userId) {
        try {
            if (!StringUtils.hasText(userId)) {
                return ResultUtils.error("用户ID不能为空");
            }
            
            if (!userDomainService.checkUserValid(userId)) {
                return ResultUtils.error("用户不存在或无效");
            }
            
            return ResultUtils.success(roomDomainService.findRoomsByMember(userId));
        } catch (Exception e) {
            return ResultUtils.error("获取用户加入房间列表失败: " + e.getMessage());
        }
    }
}