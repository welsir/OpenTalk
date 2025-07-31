package com.opentalk.domain.room.entity;

import lombok.Data;

import java.util.List;

/**
 * @author welsir
 * @description :
 * @date 2025/7/15
 */
@Data
public class PrivateRoom extends BaseRoom {

    private List<String> uids;

}