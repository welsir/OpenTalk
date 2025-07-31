package com.opentalk.domain.room.entity.valueObject;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author welsir
 * @description :
 * @date 2025/7/15
 */
@Data
public class Member {

    private String uid;
    private GroupMemberStatus status;
    private String groupNickName;

    public Member(String uid,String nickname){
        this.uid = uid;
        this.groupNickName = nickname;
        this.status = new GroupMemberStatus();
    }

    public boolean checkMemberRoomStatus() {
        return checkMemberGroupChatStatus() && checkMemberMicrophoneStatus();
    }

    public boolean checkMemberGroupChatStatus() {
        return status.isBannedChat();
    }

    public boolean checkMemberMicrophoneStatus() {
        return status.isBannedMicrophone();
    }

    public void setMemberChatStatus(Duration banTime){
        LocalDateTime now = LocalDateTime.now();
        status.banChat(now,now.plus(banTime));
    }

    public void clearMemberChatStatus(){
        status.unbanChat();
    }

    public void clearMemberMicrophoneStatus(){
        status.unbanMicrophone();
    }
}