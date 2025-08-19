package com.opentalk.domain.user.repository.persistence;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opentalk.domain.user.entity.User;
import com.opentalk.domain.user.entity.po.UserPO;
import com.opentalk.domain.user.factory.UserFactory;
import com.opentalk.domain.user.repository.facade.UserRepositoryInterface;
import com.opentalk.domain.user.repository.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author welsir
 * @description : 用户仓储实现类
 * @date 2025/7/15
 */
@Repository
public class UserRepositoryInterfaceImpl implements UserRepositoryInterface {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserFactory userFactory;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void save(User user) {
        UserPO userPO = userFactory.convertToPO(user);
        userPO.setCreateTime(LocalDateTime.now());
        userPO.setUpdateTime(LocalDateTime.now());
        userMapper.insert(userPO);
    }

    @Override
    public void update(User user) {
        UserPO userPO = userFactory.convertToPO(user);
        userPO.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(userPO);
    }

    @Override
    public User findById(String id) {
        UserPO userPO = userMapper.selectById(id);
        return userPO != null ? userFactory.convertFromPO(userPO) : null;
    }

    @Override
    public User findByUsername(String username) {
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        UserPO userPO = userMapper.selectOne(queryWrapper);
        return userPO != null ? userFactory.convertFromPO(userPO) : null;
    }

    @Override
    public User findByEmail(String email) {
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        UserPO userPO = userMapper.selectOne(queryWrapper);
        return userPO != null ? userFactory.convertFromPO(userPO) : null;
    }

    @Override
    public List<User> findByIds(List<String> ids) {
        List<UserPO> userPOs = userMapper.selectBatchIds(ids);
        return userPOs.stream()
                .map(userFactory::convertFromPO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUsername(String username) {
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        QueryWrapper<UserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        return userMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public void addFriend(String userId, String friendId) {
        UserPO userPO = userMapper.selectById(userId);
        if (userPO != null) {
            List<String> friends = parseFriends(userPO.getFriends());
            if (!friends.contains(friendId)) {
                friends.add(friendId);
                userPO.setFriends(serializeFriends(friends));
                userPO.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(userPO);
            }
        }
    }

    @Override
    public void removeFriend(String userId, String friendId) {
        UserPO userPO = userMapper.selectById(userId);
        if (userPO != null) {
            List<String> friends = parseFriends(userPO.getFriends());
            friends.remove(friendId);
            userPO.setFriends(serializeFriends(friends));
            userPO.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(userPO);
        }
    }

    @Override
    public List<User> findFriends(String userId) {
        UserPO userPO = userMapper.selectById(userId);
        if (userPO != null) {
            List<String> friendIds = parseFriends(userPO.getFriends());
            return findByIds(friendIds);
        }
        return new ArrayList<>();
    }

    @Override
    public void joinGroup(String userId, String groupId) {
        UserPO userPO = userMapper.selectById(userId);
        if (userPO != null) {
            List<String> groups = parseGroups(userPO.getJoinGroups());
            if (!groups.contains(groupId)) {
                groups.add(groupId);
                userPO.setJoinGroups(serializeGroups(groups));
                userPO.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(userPO);
            }
        }
    }

    @Override
    public void leaveGroup(String userId, String groupId) {
        UserPO userPO = userMapper.selectById(userId);
        if (userPO != null) {
            List<String> groups = parseGroups(userPO.getJoinGroups());
            groups.remove(groupId);
            userPO.setJoinGroups(serializeGroups(groups));
            userPO.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(userPO);
        }
    }

    @Override
    public List<String> findUserGroups(String userId) {
        UserPO userPO = userMapper.selectById(userId);
        return userPO != null ? parseGroups(userPO.getJoinGroups()) : new ArrayList<>();
    }

    @Override
    public void updateOnlineStatus(String userId, boolean isOnline) {
        UserPO userPO = userMapper.selectById(userId);
        if (userPO != null) {
            userPO.setOnline(isOnline);
            if (isOnline) {
                userPO.setLastLoginTime(LocalDateTime.now());
            }
            userPO.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(userPO);
        }
    }

    @Override
    public void delete(String id) {
        userMapper.deleteById(id);
    }

    // 辅助方法：解析好友列表JSON
    private List<String> parseFriends(String friendsJson) {
        if (friendsJson == null || friendsJson.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(friendsJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    // 辅助方法：序列化好友列表为JSON
    private String serializeFriends(List<String> friends) {
        try {
            return objectMapper.writeValueAsString(friends);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    // 辅助方法：解析群组列表JSON
    private List<String> parseGroups(String groupsJson) {
        if (groupsJson == null || groupsJson.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(groupsJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    // 辅助方法：序列化群组列表为JSON
    private String serializeGroups(List<String> groups) {
        try {
            return objectMapper.writeValueAsString(groups);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}