package com.opentalk.domain.user.entity;

import com.opentalk.domain.user.entity.valueObject.UserInfo;
import com.opentalk.domain.user.entity.valueObject.UserStatus;
import lombok.Data;

import java.util.List;

/**
 * @author welsir
 * @description :
 * @date 2025/7/15
 */
@Data
public class User {

    private String id;
    private String nickname;
    private String password;
    private UserInfo userInfo;
    private UserStatus userStatus;
    private List<String> joinGroups;
    private List<String> friends;
}