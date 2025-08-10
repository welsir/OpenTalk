package com.opentalk.domain.user.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author welsir
 * @description :
 * @date 2025/7/15
 */
@Data
public class UserPO {

    private String id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}