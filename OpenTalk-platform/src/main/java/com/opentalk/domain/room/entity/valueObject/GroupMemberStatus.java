package com.opentalk.domain.room.entity.valueObject;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author welsir
 * @description : 群组成员状态值对象
 * @date 2025/7/15
 */
@Data
public class GroupMemberStatus {

    private ChatBan chatBan;
    private Boolean microphoneBanned;
    private Boolean cameraBanned;
    private Boolean isOnline;
    private LocalDateTime joinTime;
    private String memberRole; // OWNER, ADMIN, MEMBER
    
    public static final String ROLE_OWNER = "OWNER";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MEMBER = "MEMBER";

    public GroupMemberStatus() {
        this.chatBan = new ChatBan();
        this.microphoneBanned = false;
        this.cameraBanned = false;
        this.isOnline = false;
        this.joinTime = LocalDateTime.now();
        this.memberRole = ROLE_MEMBER;
    }
    
    public GroupMemberStatus(String role) {
        this();
        this.memberRole = role;
    }

    public void banChat(LocalDateTime start, LocalDateTime end) {
        chatBan.setBanPeriod(start, end);
    }

    public boolean isBannedChat() {
        return chatBan.isBanned();
    }

    public void unbanChat() {
        chatBan.clear();
    }

    public void banMicrophone() {
        this.microphoneBanned = true;
    }

    public void unbanMicrophone() {
        this.microphoneBanned = false;
    }

    public boolean isBannedMicrophone() {
        return this.microphoneBanned;
    }
    
    public void banCamera() {
        this.cameraBanned = true;
    }
    
    public void unbanCamera() {
        this.cameraBanned = false;
    }
    
    public boolean isBannedCamera() {
        return this.cameraBanned;
    }
    
    public void setOnline(boolean online) {
        this.isOnline = online;
    }
    
    public boolean isOwner() {
        return ROLE_OWNER.equals(this.memberRole);
    }
    
    public boolean isAdmin() {
        return ROLE_ADMIN.equals(this.memberRole);
    }
    
    public void promoteToAdmin() {
        if (!isOwner()) {
            this.memberRole = ROLE_ADMIN;
        }
    }
    
    public void demoteToMember() {
        if (!isOwner()) {
            this.memberRole = ROLE_MEMBER;
        }
    }

    @Setter
    @Getter
    static class ChatBan{

        private LocalDateTime startTime;
        private LocalDateTime endTime;

        private ChatBan(){
            startTime = null;
            endTime = null;
        }

        private boolean isBanned() {
            if(startTime == null || endTime == null) {return false;}
            LocalDateTime now = LocalDateTime.now();
            return now.isAfter(startTime) && now.isBefore(endTime);
        }

        private void setBanPeriod(LocalDateTime start, LocalDateTime end) {
            this.startTime = start;
            this.endTime = end;
        }

        private void clear() {
            this.startTime = null;
            this.endTime = null;
        }
    }

}
