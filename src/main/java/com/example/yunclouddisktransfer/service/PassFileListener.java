package com.example.yunclouddisktransfer.service;

import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.yunclouddisktransfer.mq.StandardMessage;
import com.example.yunclouddisktransfer.mq.FileEvent;

@Service
@RocketMQMessageListener(
        topic = "file_thumbnail",
        consumerGroup = "file_thumbnail_group",
        messageModel = MessageModel.CLUSTERING
)
public class PassFileListener implements RocketMQListener<String> {

    private static final Logger logger = LoggerFactory.getLogger(PassFileListener.class);

    @Autowired
    private FileProcessor fileProcessor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onMessage(String message) {
        try {
            logger.info("Received message: {}", message);
            StandardMessage stdMsg = objectMapper.readValue(message, StandardMessage.class);
            FileEvent event = stdMsg.getContent();
            String fileType = event.getFileType();
            
            if (fileType.contains("image")) {
                logger.info("Processing file: {} (type: {})", event.getName(), fileType);
                fileProcessor.processFile(event.getName(), event.getFileHash(), event.getPhysicsFileId().toString(), fileType, event.getDownloadUrl(), event.getThumbUploadUrls());
                logger.info("Successfully processed file: {}", event.getName());
            } else {
                logger.info("Skipping file: {} (unsupported type: {})", event.getName(), fileType);
            }

        } catch (Exception e) {
            logger.error("Error processing message: {}", message, e);
        }
    }
} 