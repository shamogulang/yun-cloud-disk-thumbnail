package com.example.yunclouddisktransfer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.example.yunclouddisktransfer.entity.FileDerivative;
import com.example.yunclouddisktransfer.mapper.FileDerivativeMapper;

@Service
public class FileProcessor {
    private static final Logger logger = LoggerFactory.getLogger(FileProcessor.class);
    
    @Autowired
    private S3Service s3Service;
    @Autowired
    private ThumbnailService thumbnailService;
    @Autowired
    private FileDerivativeMapper fileDerivativeMapper;
    @Autowired
    private TranscodeService transcodeService;

    // 简单示例：处理单个 S3 文件
    public void processFile(String baseName, String fileId, String type, String downloadUrl, 
                          Map<String,String> thumbnailUrls, Map<String,String> transcodedVideoUploadUrls) throws Exception {
        File temp = null;
        try {
            if (type.contains("image")) {
                logger.info("Processing image file: {}", fileId);
                String format = "jpg";
                temp = File.createTempFile("origin-"+fileId, "." + format);
                s3Service.downloadFile(downloadUrl, temp.getAbsolutePath());
                logger.info("Downloaded image file to: {}", temp.getAbsolutePath());
                
                if (thumbnailUrls != null && !thumbnailUrls.isEmpty()) {
                    File finalTemp = temp;
                    thumbnailUrls.forEach((sizeType, uploadUrl)->{
                        try {
                            logger.info("Creating thumbnail for size: {}", sizeType);
                            File thumbFile = thumbnailService.createImageThumbnails(finalTemp, sizeType, format);
                            s3Service.uploadFile(uploadUrl, thumbFile, "image/"+format);
                            FileDerivative derivative = new FileDerivative();
                            derivative.setOriginFileId(fileId);
                            derivative.setType("thumbnail");
                            derivative.setS3Path(baseName+"-"+sizeType+"."+format);
                            derivative.setFormat(format);
                            fileDerivativeMapper.insert(derivative);
                            thumbFile.delete();
                            logger.info("Successfully created and uploaded thumbnail for size: {}", sizeType);
                        } catch (Exception e) {
                            logger.error("Error creating thumbnail for size: {}", sizeType, e);
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        } finally {
            if (temp != null && temp.exists()) {
                temp.delete();
                logger.info("Cleaned up temporary file: {}", temp.getAbsolutePath());
            }
        }
    }
} 