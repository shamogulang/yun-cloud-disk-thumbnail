package com.example.yunclouddisktransfer.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.util.regex.Pattern;

@Service
public class ThumbnailService {
    // Supported suffixes
    private static final String[] SUPPORTED_SUFFIXES = {"bmp","ilbm","png","gif","jpeg","jpg","mng","ppm","heic","webp","livp","dng"};
    private static final Pattern SUFFIX_PATTERN = Pattern.compile("(?i)\\.((bmp|ilbm|png|gif|jpeg|jpg|mng|ppm|heic|webp|livp|dng))$");
    // Supported output formats
    private static final String[] SUPPORTED_OUTPUT_FORMATS = {"webp", "jpg", "avif"};
    // Default sizes and qualities
    private static final int SIZE_S = 128, SIZE_M = 480, SIZE_L = 800, SIZE_XL = 1080;
    private static final int QUALITY_S = 70, QUALITY_M = 70, QUALITY_L = 70, QUALITY_XL = 97;
    private static final int MAX_DIM = 8192, MIN_DIM = 128, MAX_SIZE_MB = 100;

    @Value("${thumbnail.customSizeEnabled:false}")
    private boolean customSizeEnabled;

    // Utility: check suffix
    public boolean isSupportedSuffix(String filename) {
        return SUFFIX_PATTERN.matcher(filename).find();
    }
    // Utility: check file size
    public boolean isSupportedFileSize(File file) {
        return file.length() <= MAX_SIZE_MB * 1024L * 1024L;
    }
    // Utility: check dimensions
    public boolean isSupportedDimensions(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        return w >= MIN_DIM && h >= MIN_DIM && w <= MAX_DIM && h <= MAX_DIM;
    }
    // Utility: get suffix
    public String getSuffix(String filename) {
        int idx = filename.lastIndexOf('.');
        return idx >= 0 ? filename.substring(idx+1).toLowerCase() : "";
    }
    // Main entry: create all thumbnails (fixed sizes)
    public Map<String, File> createAllThumbnails(File imageFile, String origFilename, String outFormat) throws Exception {
        Map<String, File> result = new HashMap<>();
        if (!isSupportedSuffix(origFilename)) throw new IllegalArgumentException("Unsupported suffix");
        if (!isSupportedFileSize(imageFile)) throw new IllegalArgumentException("File too large");
        BufferedImage img = ImageIO.read(imageFile);
        if (img == null) throw new IllegalArgumentException("Not an image");
        if (!isSupportedDimensions(img)) throw new IllegalArgumentException("Unsupported dimensions");
        // S, M, L, XL
        int[] sizes = {SIZE_S, SIZE_M, SIZE_L, SIZE_XL};
        int[] qualities = {QUALITY_S, QUALITY_M, QUALITY_L, QUALITY_XL};
        String[] sizeNames = {"S", "M", "L", "XL"};
        for (int i = 0; i < sizes.length; i++) {
            File thumb = createThumbnailWithRules(img, sizes[i], outFormat, qualities[i]);
            if(thumb != null){
                result.put(sizeNames[i], thumb);
            }
        }
        return result;
    }
    // Core logic: create thumbnail with rules
    private File createThumbnailWithRules(BufferedImage img, int targetSize, String outFormat, int quality) throws Exception {
        int w = img.getWidth(), h = img.getHeight();
        if (w <= targetSize && h <= targetSize) {
            // Return null
            return null;
        }
        // Scale: longer edge = targetSize, keep aspect
        double scale = (double)targetSize / Math.max(w, h);
        int newW = (int)Math.round(w * scale);
        int newH = (int)Math.round(h * scale);
        // Ensure even
        if (newW % 2 != 0) newW--;
        if (newH % 2 != 0) newH--;
        File out = File.createTempFile("thumb-"+targetSize+"-", "."+outFormat);
        Thumbnails.of(img)
                .size(newW, newH)
                .outputFormat(outFormat)
                .outputQuality(quality/100.0)
                .toFile(out);
        return out;
    }
    // Stub: custom size (not public API yet)
    public File createCustomThumbnail(BufferedImage img, int w, int h, String outFormat, int quality) throws Exception {
        // Only if enabled, within bounds, not upscaling, even
        if (!customSizeEnabled) throw new UnsupportedOperationException("Custom size not enabled");
        if (w > MAX_DIM || h > MAX_DIM || w < MIN_DIM || h < MIN_DIM) throw new IllegalArgumentException("Out of bounds");
        if (w % 2 != 0 || h % 2 != 0) throw new IllegalArgumentException("Must be even");
        if (w > img.getWidth() || h > img.getHeight()) throw new IllegalArgumentException("No upscaling");
        File out = File.createTempFile("thumb-custom-", "."+outFormat);
        Thumbnails.of(img)
                .size(w, h)
                .outputFormat(outFormat)
                .outputQuality(quality/100.0)
                .toFile(out);
        return out;
    }
    // Stub: center crop (not public API yet)
    public File createCenterCropThumbnail(BufferedImage img, int w, int h, String outFormat, int quality) throws Exception {
        // Only for w,h <= 32
        if (w > 32 || h > 32) throw new IllegalArgumentException("Only for <=32");
        // Scale short edge, crop center
        double scale = Math.max((double)w/img.getWidth(), (double)h/img.getHeight());
        int scaledW = (int)Math.round(img.getWidth()*scale);
        int scaledH = (int)Math.round(img.getHeight()*scale);
        BufferedImage scaled = Thumbnails.of(img).size(scaledW, scaledH).asBufferedImage();
        int x = (scaledW - w) / 2;
        int y = (scaledH - h) / 2;
        BufferedImage cropped = scaled.getSubimage(x, y, w, h);
        File out = File.createTempFile("thumb-crop-", "."+outFormat);
        ImageIO.write(cropped, outFormat, out);
        return out;
    }
    // Stub: LIVP extraction
    public void extractLivp(File livpFile) {
        // TODO: implement extraction of image (jpg/heic) and video (mov) from LIVP
        throw new UnsupportedOperationException("LIVP extraction not implemented");
    }
}