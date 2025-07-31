package com.opentalk.interfaces.facade;

import com.opentalk.common.result.Result;
import com.opentalk.common.result.ResultUtils;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author welsir
 * @description :
 * @date 2025/7/28
 */
@RequestMapping("/openTalk/user")
public class UserApi {

    @RequestMapping("/login")
    public Result<?> login(){


        return ResultUtils.success();
    }

    @RequestMapping("/registry")
    public Result<?> registry(){

        return ResultUtils.success();
    }


}