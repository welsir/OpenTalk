package com.opentalk.domain.room.service;

import com.opentalk.common.entity.req.CreateRoomRequest;
import com.opentalk.domain.room.entity.GroupRoom;
import com.opentalk.domain.room.entity.valueObject.GroupStatus;
import com.opentalk.domain.room.factory.RoomFactory;
import com.opentalk.domain.room.repository.facade.RoomRepositoryInterface;
import com.opentalk.domain.room.repository.po.GroupRoomPO;
import com.opentalk.domain.user.entity.User;
import com.opentalk.domain.user.service.UserDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 创建房间
     */
    public GroupRoom createRoom(CreateRoomRequest request) {
        // 验证用户是否有效
        boolean status = userDomainService.checkUserValid(request.getOwnerUid());
        if (!status) {
            throw new RuntimeException("用户不存在或无效");
        }
        
        // 验证房间名称
        if (!StringUtils.hasText(request.getRoomName())) {
            throw new RuntimeException("房间名称不能为空");
        }
        
        // 检查房间名称是否已存在
        if (roomRepository.existsByName(request.getRoomName())) {
            throw new RuntimeException("房间名称已存在");
        }
        
        // 创建房间
        GroupRoom room = roomFactory.createGroupRoom(request.getRoomName(), request.getOwnerUid(), request.getOwnerName());
        
        // 保存房间
        roomRepository.save(room);
        
        log.info("创建房间成功: roomId={}, roomName={}, ownerUid={}", room.getGroupId(), room.getGroupName(), room.getOwnerUid());
        return room;
    }

    /**
     * 根据房间ID查找房间
     */
    public GroupRoom findRoomByRoomId(String roomId) {
        if (!StringUtils.hasText(roomId)) {
            throw new RuntimeException("房间ID不能为空");
        }
        
        GroupRoom room = roomRepository.findRoomById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }
        
        return room;
    }
    
    /**
     * 根据用户ID查找用户拥有的房间
     */
    public List<GroupRoom> findRoomsByOwner(String ownerUid) {
        if (!StringUtils.hasText(ownerUid)) {
            throw new RuntimeException("用户ID不能为空");
        }
        
        return roomRepository.findRoomsByOwner(ownerUid);
    }
    
    /**
     * 根据用户ID查找用户加入的房间
     */
    public List<GroupRoom> findRoomsByMember(String memberUid) {
        if (!StringUtils.hasText(memberUid)) {
            throw new RuntimeException("用户ID不能为空");
        }
        
        return roomRepository.findRoomsByMember(memberUid);
    }

    /**
     * 移除房间成员
     */
    public void removeRoomMember(String uid, String roomId) {
        if (!StringUtils.hasText(uid) || !StringUtils.hasText(roomId)) {
            throw new RuntimeException("用户ID和房间ID不能为空");
        }
        
        GroupRoom room = roomRepository.findRoomById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }

        room.removeMember(uid);
        
        // 更新房间信息
        roomRepository.update(room);
        
        // 从仓储层移除成员关系
        roomRepository.removeMember(roomId, uid);
        
        log.info("移除房间成员成功: roomId={}, uid={}", roomId, uid);
    }

    /**
     * 加入群组房间
     */
    public void joinGroupRoom(String uid, String roomId) {
        if (!StringUtils.hasText(uid) || !StringUtils.hasText(roomId)) {
            throw new RuntimeException("用户ID和房间ID不能为空");
        }
        
        GroupRoom room = roomRepository.findRoomById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }
        
        // 检查房间状态
        if (GroupStatus.DESTROYED.equals(room.getStatus())) {
            throw new RuntimeException("房间已被销毁，无法加入");
        }
        
        // 检查是否已经是成员
        List<String> members = roomRepository.getRoomMembers(roomId);
        if (members.contains(uid)) {
            throw new RuntimeException("用户已经是房间成员");
        }
        
        // 检查房间人数限制 (假设默认最大成员数为500)
        if (members.size() >= 500) {
            throw new RuntimeException("房间人数已满");
        }

        User user = userDomainService.findById(uid);
        room.addMember(uid, user.getNickname());
        
        // 更新房间信息
        roomRepository.update(room);
        
        // 在仓储层添加成员关系
        roomRepository.addMember(roomId, uid);
        
        log.info("加入房间成功: roomId={}, uid={}, nickname={}", roomId, uid, user.getNickname());
    }
    
    /**
     * 更新房间信息
     */
    public void updateRoomInfo(String roomId, String roomName, String description, String avatar, String notify) {
        if (!StringUtils.hasText(roomId)) {
            throw new RuntimeException("房间ID不能为空");
        }
        
        GroupRoom room = roomRepository.findRoomById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }
        
        // 更新房间信息
        if (StringUtils.hasText(roomName)) {
            room.setGroupName(roomName);
        }
        if (room.getGroupInfo() != null) {
            if (StringUtils.hasText(description)) {
                room.getGroupInfo().setDescription(description);
            }
            if (StringUtils.hasText(avatar)) {
                room.getGroupInfo().setAvatar(avatar);
            }
            if (StringUtils.hasText(notify)) {
                room.getGroupInfo().setNotify(notify);
            }
        }
        
        roomRepository.update(room);
        log.info("更新房间信息成功: roomId={}", roomId);
    }
    
    /**
     * 销毁房间
     */
    public void destroyRoom(String roomId, String operatorUid) {
        if (!StringUtils.hasText(roomId) || !StringUtils.hasText(operatorUid)) {
            throw new RuntimeException("房间ID和操作者ID不能为空");
        }
        
        GroupRoom room = roomRepository.findRoomById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }
        
        // 检查操作权限（只有房主可以销毁房间）
        if (!room.getOwnerUid().equals(operatorUid)) {
            throw new RuntimeException("只有房主可以销毁房间");
        }
        
        // 更新房间状态为已销毁
        room.setStatus(GroupStatus.DESTROYED);
        
        roomRepository.updateRoomStatus(roomId, GroupStatus.DESTROYED);
        log.info("销毁房间成功: roomId={}, operatorUid={}", roomId, operatorUid);
    }
    
    /**
     * 禁言成员
     */
    public void banMemberChat(String roomId, String memberUid, String operatorUid, Duration banDuration) {
        if (!StringUtils.hasText(roomId) || !StringUtils.hasText(memberUid) || !StringUtils.hasText(operatorUid)) {
            throw new RuntimeException("房间ID、成员ID和操作者ID不能为空");
        }
        
        GroupRoom room = roomRepository.findRoomById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }
        
        // 检查操作权限（只有房主可以禁言）
        if (!room.getOwnerUid().equals(operatorUid)) {
            throw new RuntimeException("只有房主可以禁言成员");
        }
        
        room.banMemberChatStatus(memberUid, banDuration);
        
        // 更新房间信息
        roomRepository.update(room);
        
        log.info("禁言成员成功: roomId={}, memberUid={}, operatorUid={}, duration={}", 
                roomId, memberUid, operatorUid, banDuration);
    }
    
    /**
     * 解除禁言
     */
    public void unbanMemberChat(String roomId, String memberUid, String operatorUid) {
        if (!StringUtils.hasText(roomId) || !StringUtils.hasText(memberUid) || !StringUtils.hasText(operatorUid)) {
            throw new RuntimeException("房间ID、成员ID和操作者ID不能为空");
        }
        
        GroupRoom room = roomRepository.findRoomById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }
        
        // 检查操作权限
        if (!room.getOwnerUid().equals(operatorUid)) {
            throw new RuntimeException("只有房主可以解除禁言");
        }
        
        room.unbanMemberChatStatus(memberUid);
        
        // 更新房间信息
        roomRepository.update(room);
        
        log.info("解除禁言成功: roomId={}, memberUid={}, operatorUid={}", roomId, memberUid, operatorUid);
    }

}