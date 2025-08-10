package com.opentalk.domain.user.service;

import com.opentalk.domain.user.entity.User;
import com.opentalk.domain.user.entity.po.UserPO;
import com.opentalk.domain.user.repository.facade.UserRepositoryInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author welsir
 * @description :
 * @date 2025/7/29
 */
@Service
@Slf4j
public class UserDomainService {


    @Resource
    UserRepositoryInterface userRepository;

    public boolean checkUserValid(String uid){
        return true;
    }

    public User findById(String uid) {
        User user = userRepository.findById(uid);
        return user;
    }


}