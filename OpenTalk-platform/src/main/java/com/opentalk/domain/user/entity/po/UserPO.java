package com.opentalk.domain.user.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author welsir
 * @description : 用户持久化对象
 * @date 2025/7/15
 */
@Data
public class UserPO {

    private String id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private boolean sex; // true: 男, false: 女
    private String status; // NORMAL, BANNED, OFFLINE, ONLINE
    private boolean isOnline;
    private LocalDateTime lastLoginTime;
    private String joinGroups; // JSON字符串存储群组ID列表
    private String friends; // JSON字符串存储好友ID列表
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}