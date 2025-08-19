package com.opentalk.interfaces.facade;

import com.opentalk.application.service.UserApplicationService;
import com.opentalk.common.result.Result;
import com.opentalk.interfaces.dto.LoginRequest;
import com.opentalk.interfaces.dto.RegisterRequest;
import com.opentalk.interfaces.dto.UpdateUserInfoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author welsir
 * @description : 用户API接口
 * @date 2025/7/28
 */
@RestController
@RequestMapping("/openTalk/user")
public class UserApi {

    @Autowired
    private UserApplicationService userApplicationService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest request) {
        return userApplicationService.loginUser(request.getUsername(), request.getPassword());
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterRequest request) {
        return userApplicationService.registerUser(
            request.getUsername(), 
            request.getPassword(), 
            request.getNickname(), 
            request.getEmail()
        );
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<?> logout(@RequestParam String userId) {
        return userApplicationService.logoutUser(userId);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public Result<?> getUserInfo(@RequestParam String userId) {
        return userApplicationService.getUserInfo(userId);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public Result<?> updateUserInfo(@RequestBody UpdateUserInfoRequest request) {
        return userApplicationService.updateUserInfo(
            request.getUserId(),
            request.getNickname(),
            request.getAvatar(),
            request.getEmail(),
            request.getPhone()
        );
    }

    /**
     * 添加好友
     */
    @PostMapping("/friend/add")
    public Result<?> addFriend(@RequestParam String userId, @RequestParam String friendId) {
        return userApplicationService.addFriend(userId, friendId);
    }

    /**
     * 删除好友
     */
    @DeleteMapping("/friend/remove")
    public Result<?> removeFriend(@RequestParam String userId, @RequestParam String friendId) {
        return userApplicationService.removeFriend(userId, friendId);
    }

    /**
     * 获取好友列表
     */
    @GetMapping("/friends")
    public Result<?> getFriendList(@RequestParam String userId) {
        return userApplicationService.getFriendList(userId);
    }

    /**
     * 搜索用户
     */
    @GetMapping("/search")
    public Result<?> searchUser(@RequestParam String keyword) {
        return userApplicationService.searchUser(keyword);
    }
}