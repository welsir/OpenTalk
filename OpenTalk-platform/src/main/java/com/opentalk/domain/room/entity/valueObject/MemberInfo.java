package com.opentalk.domain.room.entity.valueObject;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.HashMap;

/**
 * @author welsir
 * @description :
 * @date 2025/7/15
 */
@Data
@Slf4j
public class MemberInfo {

    private HashMap<String,Member> members;

    public MemberInfo() {
        members = new HashMap<>();
    }

    public boolean getMemberChatStatus(String uid) {
        Member member = members.get(uid);
        return member.checkMemberGroupChatStatus();
    }

    public boolean getMemberMicrophoneStatus(String uid) {
        Member member = members.get(uid);
        return member.checkMemberMicrophoneStatus();
    }

    public void setMemberChatStatus(String uid, Duration time) {
        Member member = members.get(uid);
        member.setMemberChatStatus(time);
    }

    public void clearMemberChatStatus(String uid) {
        Member member = members.get(uid);
        member.clearMemberChatStatus();
    }
    public void addMember(String uid,String nickname) {
        Member member = new Member(uid, nickname);
        members.put(uid,member);
    }

    public void removeMember(String uid) {
        members.remove(uid);
    }

    public boolean containsMember(String uid) {
        return members.containsKey(uid);
    }

    public void setMemberMicrophoneStatus(String uid) {
        Member member = members.get(uid);
        member.clearMemberMicrophoneStatus();
    }

    public void clearMemberMicrophoneStatus(String uid) {
        Member member = members.get(uid);
        member.clearMemberMicrophoneStatus();
    }
}