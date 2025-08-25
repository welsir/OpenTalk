package com.opentalk.domain.user.factory;

import com.opentalk.domain.user.entity.User;
import com.opentalk.domain.user.entity.po.UserPO;
import com.opentalk.domain.user.entity.valueObject.UserInfo;
import com.opentalk.domain.user.entity.valueObject.UserStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author welsir
 * @description : 用户工厂类
 * @date 2025/7/15
 */
@Component
public class UserFactory {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建新用户
     */
    public User createUser(String username, String password, String nickname) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setNickname(nickname);
        user.setPassword(password);
        
        // 初始化用户信息
        UserInfo userInfo = new UserInfo();
        user.setUserInfo(userInfo);
        
        // 初始化用户状态
        UserStatus userStatus = new UserStatus();
        user.setUserStatus(userStatus);
        
        // 初始化空的群组和好友列表
        user.setJoinGroups(new ArrayList<>());
        user.setFriends(new ArrayList<>());
        
        return user;
    }
    
    /**
     * 从PO转换为领域对象
     */
    public User convertFromPO(UserPO userPO) {
        if (userPO == null) {
            return null;
        }
        
        User user = new User();
        user.setId(userPO.getId());
        user.setUsername(userPO.getUsername());
        user.setNickname(userPO.getNickname());
        user.setPassword(userPO.getPassword());
        
        // 转换用户信息
        UserInfo userInfo = new UserInfo();
        userInfo.setEmail(userPO.getEmail());
        userInfo.setPhone(userPO.getPhone());
        userInfo.setAvatar(userPO.getAvatar());
        userInfo.setSex(userPO.isSex());
        user.setUserInfo(userInfo);
        
        // 转换用户状态
        UserStatus userStatus = new UserStatus(userPO.getStatus());
        userStatus.setOnline(userPO.isOnline());
        userStatus.setLastLoginTime(userPO.getLastLoginTime());
        userStatus.setCreateTime(userPO.getCreateTime());
        user.setUserStatus(userStatus);
        
        // 转换群组和好友列表
        try {
            if (userPO.getJoinGroups() != null) {
                List<String> joinGroups = objectMapper.readValue(userPO.getJoinGroups(), new TypeReference<List<String>>() {});
                user.setJoinGroups(joinGroups);
            } else {
                user.setJoinGroups(new ArrayList<>());
            }
            
            if (userPO.getFriends() != null) {
                List<String> friends = objectMapper.readValue(userPO.getFriends(), new TypeReference<List<String>>() {});
                user.setFriends(friends);
            } else {
                user.setFriends(new ArrayList<>());
            }
        } catch (Exception e) {
            user.setJoinGroups(new ArrayList<>());
            user.setFriends(new ArrayList<>());
        }
        
        return user;
    }
    
    /**
     * 从领域对象转换为PO
     */
    public UserPO convertToPO(User user) {
        if (user == null) {
            return null;
        }
        
        UserPO userPO = new UserPO();
        userPO.setId(user.getId());
        userPO.setUsername(user.getUsername());
        userPO.setNickname(user.getNickname());
        userPO.setPassword(user.getPassword());
        
        // 转换用户信息
        if (user.getUserInfo() != null) {
            userPO.setEmail(user.getUserInfo().getEmail());
            userPO.setPhone(user.getUserInfo().getPhone());
            userPO.setAvatar(user.getUserInfo().getAvatar());
            userPO.setSex(user.getUserInfo().isSex());
        }
        
        // 转换用户状态
        if (user.getUserStatus() != null) {
            userPO.setStatus(user.getUserStatus().getStatus());
            userPO.setOnline(user.getUserStatus().isOnline());
            userPO.setLastLoginTime(user.getUserStatus().getLastLoginTime());
            userPO.setCreateTime(user.getUserStatus().getCreateTime());
        }
        
        // 转换群组和好友列表为JSON字符串
        try {
            if (user.getJoinGroups() != null) {
                userPO.setJoinGroups(objectMapper.writeValueAsString(user.getJoinGroups()));
            }
            if (user.getFriends() != null) {
                userPO.setFriends(objectMapper.writeValueAsString(user.getFriends()));
            }
        } catch (Exception e) {
            // 处理JSON转换异常
            userPO.setJoinGroups("[]");
            userPO.setFriends("[]");
        }
        
        return userPO;
    }
}