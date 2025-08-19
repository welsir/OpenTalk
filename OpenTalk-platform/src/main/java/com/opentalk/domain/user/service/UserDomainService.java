package com.opentalk.domain.user.service;

import com.opentalk.domain.user.entity.User;
import com.opentalk.domain.user.entity.valueObject.UserStatus;
import com.opentalk.domain.user.factory.UserFactory;
import com.opentalk.domain.user.repository.facade.UserRepositoryInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author welsir
 * @description : 用户领域服务
 * @date 2025/7/29
 */
@Service
@Slf4j
public class UserDomainService {

    @Resource
    UserRepositoryInterface userRepository;
    
    @Resource
    UserFactory userFactory;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 检查用户是否有效
     */
    public boolean checkUserValid(String uid){
        if (!StringUtils.hasText(uid)) {
            return false;
        }
        User user = userRepository.findById(uid);
        return user != null && user.getUserStatus() != null && user.getUserStatus().isNormal();
    }

    /**
     * 根据ID查找用户
     */
    public User findById(String uid) {
        return userRepository.findById(uid);
    }
    
    /**
     * 用户注册
     */
    public User registerUser(String username, String password, String nickname, String email) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (StringUtils.hasText(email) && userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已被注册");
        }
        
        // 创建用户
        User user = userFactory.createUser(username, encryptPassword(password), nickname);
        if (StringUtils.hasText(email)) {
            user.getUserInfo().setEmail(email);
        }
        
        // 保存用户
        userRepository.save(user);
        log.info("用户注册成功: {}", username);
        return user;
    }
    
    /**
     * 用户登录验证
     */
    public User authenticateUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (user.getUserStatus().isBanned()) {
            throw new RuntimeException("用户已被禁用");
        }
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        // 更新登录状态
        user.getUserStatus().login();
        userRepository.update(user);
        userRepository.updateOnlineStatus(user.getId(), true);
        
        log.info("用户登录成功: {}", username);
        return user;
    }
    
    /**
     * 用户登出
     */
    public void logoutUser(String userId) {
        User user = userRepository.findById(userId);
        if (user != null) {
            user.getUserStatus().logout();
            userRepository.update(user);
            userRepository.updateOnlineStatus(userId, false);
            log.info("用户登出: {}", userId);
        }
    }
    
    /**
     * 更新用户信息
     */
    public void updateUserInfo(String userId, String nickname, String avatar, String email, String phone) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (StringUtils.hasText(nickname)) {
            user.setNickname(nickname);
        }
        
        if (user.getUserInfo() != null) {
            if (StringUtils.hasText(avatar)) {
                user.getUserInfo().setAvatar(avatar);
            }
            if (StringUtils.hasText(email)) {
                user.getUserInfo().setEmail(email);
            }
            if (StringUtils.hasText(phone)) {
                user.getUserInfo().setPhone(phone);
            }
        }
        
        userRepository.update(user);
        log.info("用户信息更新成功: {}", userId);
    }
    
    /**
     * 添加好友
     */
    public void addFriend(String userId, String friendId) {
        if (userId.equals(friendId)) {
            throw new RuntimeException("不能添加自己为好友");
        }
        
        User user = userRepository.findById(userId);
        User friend = userRepository.findById(friendId);
        
        if (user == null || friend == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (user.getFriends().contains(friendId)) {
            throw new RuntimeException("已经是好友关系");
        }
        
        userRepository.addFriend(userId, friendId);
        userRepository.addFriend(friendId, userId); // 双向好友关系
        
        log.info("添加好友成功: {} -> {}", userId, friendId);
    }
    
    /**
     * 删除好友
     */
    public void removeFriend(String userId, String friendId) {
        userRepository.removeFriend(userId, friendId);
        userRepository.removeFriend(friendId, userId); // 双向删除
        
        log.info("删除好友成功: {} -> {}", userId, friendId);
    }
    
    /**
     * 获取用户好友列表
     */
    public List<User> getUserFriends(String userId) {
        return userRepository.findFriends(userId);
    }
    
    /**
     * 加密密码
     */
    private String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }
}