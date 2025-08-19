package com.opentalk.domain.room.repository.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author welsir
 * @description : 群组房间持久化对象
 * @date 2025/7/29
 */
@Data
public class GroupRoomPO {

    private String roomId;
    private String roomName;
    private String ownerUid;
    private String status; // NORMAL, DESTROYED
    private String roomType; // GROUP, PRIVATE
    private String description; // 群组描述
    private String avatar; // 群组头像
    private String notify; // 群公告
    private String members; // JSON字符串存储成员信息
    private String messageIds; // JSON字符串存储消息ID列表
    private int maxMembers; // 最大成员数
    private int currentMembers; // 当前成员数
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}