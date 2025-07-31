package com.example.yunclouddisktransfer.utils;


import java.util.ArrayList;
import java.util.List;

public enum VideoQuality {
    SD(854, 480, "SD", "480p"),
    HD(1280, 720, "HD", "720p"),
    FULL_HD(1920, 1080, "FHD", "1080p"),
    UHD_4K(3840, 2160, "4K", "2160p");

    private final int width;
    private final int height;
    private final String code;
    private final String label;

    VideoQuality(int width, int height, String code, String label) {
        this.width = width;
        this.height = height;
        this.code = code;
        this.label = label;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getCode() { return code; }
    public String getLabel() { return label; }

    public static VideoQuality fromHeight(int height) {
        for (VideoQuality size : values()) {
            if (size.height == height) {
                return size;
            }
        }
        throw new IllegalArgumentException("Unknown resolution: " + height);
    }

    public static List<VideoQuality> getCompatibleQualities(int sourceWidth, int sourceHeight) {
        List<VideoQuality> result = new ArrayList<>();
        for (VideoQuality quality : values()) {
            if (quality.width <= sourceWidth && quality.height <= sourceHeight) {
                result.add(quality);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return label + " (" + width + "x" + height + ")";
    }
}
