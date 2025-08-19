package com.opentalk.domain.room.repository.facade;

import com.opentalk.domain.room.entity.GroupRoom;
import com.opentalk.domain.room.repository.po.GroupRoomPO;

import java.util.List;

/**
 * @author welsir
 * @description : 房间仓储接口
 * @date 2025/7/29
 */
public interface RoomRepositoryInterface {

    GroupRoomPO findById(String roomId);
    
    GroupRoom findRoomById(String roomId);
    
    List<GroupRoom> findRoomsByOwner(String ownerUid);
    
    List<GroupRoom> findRoomsByMember(String memberUid);
    
    List<GroupRoom> findPublicRooms(int limit);
    
    boolean existsById(String roomId);
    
    boolean existsByName(String roomName);

    void save(GroupRoom groupRoom);
    
    void update(GroupRoom groupRoom);
    
    void delete(String roomId);
    
    void addMember(String roomId, String memberId);
    
    void removeMember(String roomId, String memberId);
    
    void updateMemberCount(String roomId, int count);
    
    void updateRoomStatus(String roomId, String status);
    
    int getMemberCount(String roomId);
    
    List<String> getRoomMembers(String roomId);
}