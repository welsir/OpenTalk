package com.opentalk.domain.user.entity.valueObject;

import lombok.Data;

/**
 * @author welsir
 * @description :
 * @date 2025/7/15
 */
@Data
public class UserInfo {

    private String email;
    private String phone;
    private String avatar;
    private boolean sex;

}