package com.opentalk.domain.user.repository.facade;

import com.opentalk.domain.user.entity.User;

import java.util.List;

/**
 * @author welsir
 * @description : 用户仓储接口
 * @date 2025/7/15
 */
public interface UserRepositoryInterface {

    void save(User user);
    
    void update(User user);

    User findById(String id);
    
    User findByUsername(String username);
    
    User findByEmail(String email);
    
    List<User> findByIds(List<String> ids);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);

    void addFriend(String userId, String friendId);
    
    void removeFriend(String userId, String friendId);
    
    List<User> findFriends(String userId);
    
    void joinGroup(String userId, String groupId);
    
    void leaveGroup(String userId, String groupId);
    
    List<String> findUserGroups(String userId);
    
    void updateOnlineStatus(String userId, boolean isOnline);
    
    void delete(String id);
}