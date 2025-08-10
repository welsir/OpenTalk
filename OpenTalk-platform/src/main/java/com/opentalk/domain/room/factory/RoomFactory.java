package com.opentalk.domain.room.factory;

import com.opentalk.domain.room.entity.GroupRoom;
import com.opentalk.domain.room.entity.valueObject.GroupInfo;
import com.opentalk.domain.room.entity.valueObject.MemberInfo;
import com.opentalk.domain.room.repository.po.GroupRoomPO;
import org.springframework.stereotype.Service;

/**
 * @author welsir
 * @description :
 * @date 2025/7/29
 */
@Service
public class RoomFactory {

    public GroupRoom convertGroupRoom(GroupRoomPO roomPO) {

        GroupRoom room = new GroupRoom(roomPO.getRoomName(),roomPO.getOwnerUid());

        return room;
    }


    public GroupRoom createGroupRoom(String roomName,String ownerUid,String nickName) {

        GroupInfo groupInfo = createGroupInfo();
        MemberInfo memberInfo = initMemberInfo(ownerUid, nickName);
        return new GroupRoom(roomName,ownerUid, groupInfo,memberInfo);
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