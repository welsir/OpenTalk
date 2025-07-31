package com.opentalk.domain.user.repository.persistence;

import com.opentalk.domain.user.entity.User;
import com.opentalk.domain.user.repository.facade.UserRepositoryInterface;
import org.springframework.stereotype.Repository;

/**
 * @author welsir
 * @description :
 * @date 2025/7/15
 */
@Repository
public class UserRepositoryInterfaceImpl implements UserRepositoryInterface {


    @Override
    public void save(User user) {

    }

    @Override
    public User findById(String id) {
        return null;
    }

    @Override
    public void addFriend(User user) {

    }
}