package com.opentalk.domain.room.entity;

import com.opentalk.domain.room.entity.valueObject.GroupInfo;
import com.opentalk.domain.room.entity.valueObject.GroupStatus;
import com.opentalk.domain.room.entity.valueObject.MemberInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * @author welsir
 * @description :
 * @date 2025/7/15
 */
@Data
@Slf4j
public class GroupRoom extends BaseRoom{

    private String groupName;
    private GroupStatus status;
    private GroupInfo groupInfo;
    private MemberInfo memberInfo;

    public void addMember(String uid,String username){
        if(memberInfo.containsMember(uid)){
            log.error("member is exist :{}",uid);
            return;
        }
        memberInfo.addMember(uid,username);
    }

    public void removeMember(String uid){
        memberInfo.removeMember(uid);
    }

    public void banMemberChatStatus(String uid, Duration ban){
        if(!memberInfo.containsMember(uid)){
            log.error("member not exist :{}",uid);
            throw new RuntimeException("member not exist");
        }

        memberInfo.setMemberChatStatus(uid,ban);
    }

    public void unbanMemberChatStatus(String uid){
        memberInfo.clearMemberChatStatus(uid);
    }

    public void banMemberMicrophoneStatus(String uid){
        if(!memberInfo.containsMember(uid)){
            log.error("member not exist :{}",uid);
            throw new RuntimeException("member not exist");
        }

        memberInfo.setMemberMicrophoneStatus(uid);
    }

    public void unbanMemberMicrophoneStatus(String uid){
        if(!memberInfo.containsMember(uid)){
            log.error("member not exist :{}",uid);
            throw new RuntimeException("member not exist");
        }

        memberInfo.clearMemberMicrophoneStatus(uid);
    }
}