package com.opentalk.domain.message;

import com.opentalk.domain.message.valueObject.MessageType;
import lombok.Data;

/**
 * @author welsir
 * @description :
 * @date 2025/7/15
 */
@Data
public class Message {

    private String id;
    private String content;
    private MessageType type;

}