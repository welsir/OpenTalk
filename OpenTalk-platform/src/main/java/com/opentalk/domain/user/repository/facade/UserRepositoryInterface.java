package com.opentalk.domain.user.repository.facade;

import com.opentalk.domain.user.entity.User;

/**
 * @author welsir
 * @description :
 * @date 2025/7/15
 */
public interface UserRepositoryInterface {

    void save(User user);

    User findById(String id);

    void addFriend(User user);
}