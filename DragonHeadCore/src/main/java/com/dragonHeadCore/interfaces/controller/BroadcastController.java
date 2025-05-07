package com.dragonHeadCore.interfaces.controller;


import com.common.entity.DTO.CreateRoomRequestDTO;

import com.common.result.Result;
import com.dragonHeadCore.application.service.BroadcastService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/room")
public class BroadcastController {

    @Resource
    BroadcastService broadcastService;
    /**
     * 开播
     * @param createRoomRequestDTO
     */
    @PostMapping("/create")
    public Result<?> createRoom(@Valid @RequestBody CreateRoomRequestDTO createRoomRequestDTO) {
        return broadcastService.startBroadcast();
    }

    /**
     * 关播
     */
    @PostMapping("/quit")
    public void quitRoom(){

    }
}
