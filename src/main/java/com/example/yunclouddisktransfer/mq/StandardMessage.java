package com.example.yunclouddisktransfer.mq;

import lombok.Data;

@Data
public class StandardMessage {

    String messageId;

    Long userId;
    /**
     * 发生时间, RFC 3339，2019-08-20T06:51:27.292Z
     */
    String eventTime;
    /**
     * 消息类型/动作类型
     */
    String eventType;
    /**
     * 消息版本号
     */
    String version;
    /**
     * 消息内容，json格式，由各业务消息定义
     */
    FileEvent content;
}
