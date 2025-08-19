package com.opentalk.domain.room.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opentalk.domain.room.entity.GroupRoom;
import com.opentalk.domain.room.entity.valueObject.GroupInfo;
import com.opentalk.domain.room.entity.valueObject.GroupStatus;
import com.opentalk.domain.room.entity.valueObject.MemberInfo;
import com.opentalk.domain.room.entity.valueObject.RoomType;
import com.opentalk.domain.room.repository.po.GroupRoomPO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author welsir
 * @description :
 * @date 2025/7/29
 */
@Service
public class RoomFactory {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将PO对象转换为实体对象
     */
    public GroupRoom convertGroupRoom(GroupRoomPO roomPO) {
        if (roomPO == null) {
            return null;
        }

        GroupRoom room = new GroupRoom(roomPO.getRoomName(), roomPO.getOwnerUid());
        room.setGroupId(roomPO.getRoomId());
        room.setStatus(roomPO.getStatus() != null ? roomPO.getStatus() : GroupStatus.NORMAL);
        room.setRoomType(roomPO.getRoomType() != null ? roomPO.getRoomType() : RoomType.GROUP);
        
        // 设置群组信息
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setDescription(roomPO.getDescription());
        groupInfo.setAvatar(roomPO.getAvatar());
        groupInfo.setNotify(roomPO.getNotify());
        room.setGroupInfo(groupInfo);
        
        // 设置成员信息
        MemberInfo memberInfo = new MemberInfo();
        if (roomPO.getMembers() != null && !roomPO.getMembers().isEmpty()) {
            try {
                List<String> members = objectMapper.readValue(roomPO.getMembers(), new TypeReference<List<String>>() {});
                for (String memberId : members) {
                    memberInfo.addMember(memberId, ""); // 昵称需要从其他地方获取
                }
            } catch (JsonProcessingException e) {
                // 处理JSON解析异常
            }
        }
        room.setMemberInfo(memberInfo);
        
        // 设置消息ID列表
        if (roomPO.getMessageIds() != null && !roomPO.getMessageIds().isEmpty()) {
            try {
                List<String> messageIds = objectMapper.readValue(roomPO.getMessageIds(), new TypeReference<List<String>>() {});
                room.setMessageId(messageIds);
            } catch (JsonProcessingException e) {
                room.setMessageId(new ArrayList<>());
            }
        }
        
        return room;
    }


    /**
     * 创建新的群组房间
     */
    public GroupRoom createGroupRoom(String roomName, String ownerUid, String nickName) {
        GroupInfo groupInfo = createGroupInfo();
        MemberInfo memberInfo = initMemberInfo(ownerUid, nickName);
        
        GroupRoom room = new GroupRoom(roomName, ownerUid, groupInfo, memberInfo);
        room.setGroupId(UUID.randomUUID().toString());
        room.setStatus(GroupStatus.NORMAL);
        room.setRoomType(RoomType.GROUP);
        
        return room;
    }
    
    /**
     * 将实体对象转换为PO对象
     */
    public GroupRoomPO convertToPO(GroupRoom room) {
        if (room == null) {
            return null;
        }
        
        GroupRoomPO po = new GroupRoomPO();
        po.setRoomId(room.getGroupId());
        po.setRoomName(room.getGroupName());
        po.setOwnerUid(room.getOwnerUid());
        po.setStatus(room.getStatus() != null ? room.getStatus() : GroupStatus.NORMAL);
        po.setRoomType(room.getRoomType() != null ? room.getRoomType() : RoomType.GROUP);
        po.setCreateTime(LocalDateTime.now());
        po.setUpdateTime(LocalDateTime.now());
        
        // 设置群组信息
        if (room.getGroupInfo() != null) {
            po.setDescription(room.getGroupInfo().getDescription());
            po.setAvatar(room.getGroupInfo().getAvatar());
            po.setNotify(room.getGroupInfo().getNotify());
        }
        
        // 设置成员信息
        if (room.getMemberInfo() != null && room.getMemberInfo().getMembers() != null) {
            try {
                List<String> memberIds = new ArrayList<>(room.getMemberInfo().getMembers().keySet());
                po.setMembers(objectMapper.writeValueAsString(memberIds));
                po.setCurrentMembers(memberIds.size());
            } catch (JsonProcessingException e) {
                po.setMembers("[]");
                po.setCurrentMembers(0);
            }
        } else {
            po.setMembers("[]");
            po.setCurrentMembers(0);
        }
        
        // 设置消息ID列表
        if (room.getMessageId() != null && !room.getMessageId().isEmpty()) {
            try {
                po.setMessageIds(objectMapper.writeValueAsString(room.getMessageId()));
            } catch (JsonProcessingException e) {
                po.setMessageIds("[]");
            }
        } else {
            po.setMessageIds("[]");
        }
        
        // 设置默认值
        po.setMaxMembers(500); // 默认最大成员数
        
        return po;
    }

    private GroupInfo createGroupInfo() {
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setAvatar("");
        groupInfo.setNotify("");
        groupInfo.setDescription("");
        return groupInfo;
    }

    private MemberInfo initMemberInfo(String uid, String nickname) {
        MemberInfo info = new MemberInfo();
        info.addMember(uid,nickname);
        return info;
    }

}