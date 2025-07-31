package com.example.yunclouddisktransfer.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.Map;

import com.example.yunclouddisktransfer.entity.FileDerivative;
import com.example.yunclouddisktransfer.mapper.FileDerivativeMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

@Service
public class FileProcessor {
    private static final Logger logger = LoggerFactory.getLogger(FileProcessor.class);
    
    @Autowired
    private S3Service s3Service;
    @Autowired
    private ThumbnailService thumbnailService;
    @Autowired
    private FileDerivativeMapper fileDerivativeMapper;

    public void processFile(String name,String fileHash, String fileId, String type, String downloadUrl,
                          Map<String,String> thumbnailUrls) throws Exception {
        File temp = null;
        try {
            if (type.contains("image")) {
                logger.info("Processing image file: {}", fileId);
                String format = "jpg";
                temp = File.createTempFile("origin-"+fileId, "." + format);
                s3Service.downloadFile(downloadUrl, temp.getAbsolutePath());
                logger.info("Downloaded image file to: {}", temp.getAbsolutePath());

                // Validate suffix, size, dimensions
                if (!thumbnailService.isSupportedSuffix(name)) {
                    logger.warn("Unsupported image suffix for file: {}", name);
                    return;
                }
                if (!thumbnailService.isSupportedFileSize(temp)) {
                    logger.warn("File too large for thumbnail: {}", name);
                    return;
                }
                BufferedImage img = ImageIO.read(temp);
                if (img == null) {
                    logger.warn("Not a valid image: {}", name);
                    return;
                }
                if (!thumbnailService.isSupportedDimensions(img)) {
                    logger.warn("Image dimensions not supported: {}", name);
                    return;
                }

                // Generate all four thumbnails (S, M, L, XL)
                Map<String, File> thumbs = thumbnailService.createAllThumbnails(temp, name, format);
                for (Map.Entry<String, File> entry : thumbs.entrySet()) {
                    String sizeType = entry.getKey();
                    File thumbFile = entry.getValue();
                    String uploadUrl = thumbnailUrls != null ? thumbnailUrls.get(sizeType) : null;
                    if (uploadUrl != null) {
                        s3Service.uploadFile(uploadUrl, thumbFile, "image/"+format);
                        FileDerivative derivative = new FileDerivative();
                        derivative.setPhysicalFileId(fileId);
                        derivative.setType("thumbnail");
                        derivative.setSize(getSizeForType(sizeType));
                        derivative.setS3Path(fileId +"-"+ fileHash+"-thumb-"+sizeType+"."+format);
                        derivative.setFormat(format);
                        fileDerivativeMapper.insert(derivative);
                        thumbFile.delete();
                        logger.info("Successfully created and uploaded thumbnail for size: {}", sizeType);
                    } else {
                        logger.warn("No upload URL for thumbnail size: {}", sizeType);
                    }
                }
            }
        } finally {
            if (temp != null && temp.exists()) {
                temp.delete();
                logger.info("Cleaned up temporary file: {}", temp.getAbsolutePath());
            }
        }
    }

    private Integer getSizeForType(String sizeType) {
        switch (sizeType) {
            case "S": return 128;
            case "M": return 480;
            case "L": return 800;
            case "XL": return 1080;
            default: return null;
        }
    }
} 