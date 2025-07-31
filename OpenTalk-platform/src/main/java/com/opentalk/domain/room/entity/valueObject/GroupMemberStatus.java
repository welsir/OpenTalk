package com.opentalk.domain.room.entity.valueObject;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author welsir
 * @description :
 * @date 2025/7/15
 */
@Data
public class GroupMemberStatus {

    private ChatBan chatBan;
    private Boolean microphone;

    public GroupMemberStatus() {
        this.chatBan = new ChatBan();
        microphone = false;
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
        microphone = true;
    }

    public void unbanMicrophone() {
        microphone = false;
    }

    public boolean isBannedMicrophone() {
        return microphone;
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
