package com.opentalk.application.service;

import com.opentalk.common.result.Result;
import com.opentalk.common.result.ResultUtils;
import com.opentalk.domain.user.entity.User;
import com.opentalk.domain.user.factory.UserFactory;
import com.opentalk.domain.user.repository.facade.UserRepositoryInterface;
import com.opentalk.domain.user.service.UserDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author welsir
 * @description : 用户应用服务
 * @date 2025/7/15
 */
@Service
public class UserApplicationService {

    @Autowired
    private UserDomainService userDomainService;
    
    @Autowired
    private UserRepositoryInterface userRepository;
    
    @Autowired
    private UserFactory userFactory;

    /**
     * 用户注册
     */
    public Result<?> registerUser(String username, String password, String nickname, String email) {
        try {
            // 参数校验
            if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
                return ResultUtils.error("用户名和密码不能为空");
            }
            
            // 检查用户名是否已存在
            if (userRepository.existsByUsername(username)) {
                return ResultUtils.error("用户名已存在");
            }
            
            // 检查邮箱是否已存在
            if (StringUtils.hasText(email) && userRepository.existsByEmail(email)) {
                return ResultUtils.error("邮箱已被注册");
            }
            
            // 创建用户
            User user = userDomainService.registerUser(username, password, nickname, email);
            userRepository.save(user);
            
            return ResultUtils.success("注册成功");
        } catch (Exception e) {
            return ResultUtils.error("注册失败：" + e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    public Result<?> loginUser(String username, String password) {
        try {
            // 参数校验
            if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
                return ResultUtils.error("用户名和密码不能为空");
            }
            
            // 验证用户登录
            User user = userDomainService.authenticateUser(username, password);
            
            // 更新在线状态
            userRepository.updateOnlineStatus(user.getId(), true);
            
            return ResultUtils.success(user);
        } catch (Exception e) {
            return ResultUtils.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    public Result<?> logoutUser(String userId) {
        try {
            if (!StringUtils.hasText(userId)) {
                return ResultUtils.error("用户ID不能为空");
            }
            
            // 更新离线状态
            userRepository.updateOnlineStatus(userId, false);
            
            return ResultUtils.success("登出成功");
        } catch (Exception e) {
            return ResultUtils.error("登出失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    public Result<?> updateUserInfo(String userId, String nickname, String avatar, String email, String phone) {
        try {
            if (!StringUtils.hasText(userId)) {
                return ResultUtils.error("用户ID不能为空");
            }
            
            User user = userRepository.findById(userId);
            if (user == null) {
                return ResultUtils.error("用户不存在");
            }
            
            // 更新用户信息
            userDomainService.updateUserInfo(userId, nickname, avatar, email, phone);
            
            // 重新获取更新后的用户信息
            User updatedUser = userRepository.findById(userId);
            return ResultUtils.success(updatedUser);
        } catch (Exception e) {
            return ResultUtils.error("更新用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户信息
     */
    public Result<?> getUserInfo(String userId) {
        try {
            if (!StringUtils.hasText(userId)) {
                return ResultUtils.error("用户ID不能为空");
            }
            
            User user = userRepository.findById(userId);
            if (user == null) {
                return ResultUtils.error("用户不存在");
            }
            
            return ResultUtils.success(user);
        } catch (Exception e) {
            return ResultUtils.error("获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 添加好友
     */
    public Result<?> addFriend(String userId, String friendId) {
        try {
            if (!StringUtils.hasText(userId) || !StringUtils.hasText(friendId)) {
                return ResultUtils.error("用户ID不能为空");
            }
            
            if (userId.equals(friendId)) {
                return ResultUtils.error("不能添加自己为好友");
            }
            
            // 检查用户是否存在
            User user = userRepository.findById(userId);
            User friend = userRepository.findById(friendId);
            if (user == null || friend == null) {
                return ResultUtils.error("用户不存在");
            }
            
            // 添加好友关系
            userDomainService.addFriend(userId, friendId);
            userRepository.addFriend(userId, friendId);
            userRepository.addFriend(friendId, userId); // 双向好友关系
            
            return ResultUtils.success("添加好友成功");
        } catch (Exception e) {
            return ResultUtils.error("添加好友失败：" + e.getMessage());
        }
    }

    /**
     * 删除好友
     */
    public Result<?> removeFriend(String userId, String friendId) {
        try {
            if (!StringUtils.hasText(userId) || !StringUtils.hasText(friendId)) {
                return ResultUtils.error("用户ID不能为空");
            }
            
            // 删除好友关系
            userDomainService.removeFriend(userId, friendId);
            userRepository.removeFriend(userId, friendId);
            userRepository.removeFriend(friendId, userId); // 双向删除
            
            return ResultUtils.success("删除好友成功");
        } catch (Exception e) {
            return ResultUtils.error("删除好友失败：" + e.getMessage());
        }
    }

    /**
     * 获取好友列表
     */
    public Result<?> getFriendList(String userId) {
        try {
            if (!StringUtils.hasText(userId)) {
                return ResultUtils.error("用户ID不能为空");
            }
            
            List<User> friends = userRepository.findFriends(userId);
            return ResultUtils.success(friends);
        } catch (Exception e) {
            return ResultUtils.error("获取好友列表失败：" + e.getMessage());
        }
    }

    /**
     * 搜索用户
     */
    public Result<?> searchUser(String keyword) {
        try {
            if (!StringUtils.hasText(keyword)) {
                return ResultUtils.error("搜索关键词不能为空");
            }
            
            // 先按用户名搜索
            User user = userRepository.findByUsername(keyword);
            if (user != null) {
                return ResultUtils.success(user);
            }
            
            // 再按邮箱搜索
            user = userRepository.findByEmail(keyword);
            if (user != null) {
                return ResultUtils.success(user);
            }
            
            return ResultUtils.error("未找到用户");
        } catch (Exception e) {
            return ResultUtils.error("搜索用户失败：" + e.getMessage());
        }
    }
}