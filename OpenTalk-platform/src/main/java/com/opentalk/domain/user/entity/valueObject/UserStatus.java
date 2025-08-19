package com.opentalk.domain.user.entity.valueObject;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author welsir
 * @description : 用户状态值对象
 * @date 2025/7/15
 */
@Data
public class UserStatus {

    public static final String NORMAL = "normal";
    public static final String BANNED = "banned";
    public static final String OFFLINE = "offline";
    public static final String ONLINE = "online";
    
    private String status;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
    private boolean isOnline;
    
    public UserStatus() {
        this.status = NORMAL;
        this.createTime = LocalDateTime.now();
        this.isOnline = false;
    }
    
    public UserStatus(String status) {
        this.status = status;
        this.createTime = LocalDateTime.now();
        this.isOnline = false;
    }
    
    public void login() {
        this.isOnline = true;
        this.lastLoginTime = LocalDateTime.now();
    }
    
    public void logout() {
        this.isOnline = false;
    }
    
    public void banUser() {
        this.status = BANNED;
        this.isOnline = false;
    }
    
    public void unbanUser() {
        this.status = NORMAL;
    }
    
    public boolean isNormal() {
        return NORMAL.equals(this.status);
    }
    
    public boolean isBanned() {
        return BANNED.equals(this.status);
    }
}