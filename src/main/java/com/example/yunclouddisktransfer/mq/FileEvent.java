package com.example.yunclouddisktransfer.mq;

import lombok.Data;

import java.util.Map;

@Data
public class FileEvent {
    private String fullFileIdPath;
    private String fileHash;
    private String createdAt;
    private Long parentFileId;
    private String filePosition;
    private String name;
    private String fileType;
    private Long fileId;
    private Long physicsFileId;
    private String updatedAt;
    private String userId;
    private String downloadUrl;
    private Map<String, String> thumbUploadUrls;
    private String baseName;
    private Map<String, String> transcodeVideoUploadUrls;
}