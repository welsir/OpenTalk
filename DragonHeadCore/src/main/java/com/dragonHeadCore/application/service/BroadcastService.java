package com.dragonHeadCore.application.service;

import com.common.entity.DTO.StartBroadcastResponseDTO;
import com.common.result.Result;

public interface BroadcastService {

    Result<StartBroadcastResponseDTO> startBroadcast();

}
