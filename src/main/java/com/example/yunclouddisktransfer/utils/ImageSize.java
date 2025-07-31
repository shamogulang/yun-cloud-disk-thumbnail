package com.example.yunclouddisktransfer.utils;

public enum ImageSize {
    S(128, "S"),
    M(480, "M"),
    L(800, "L"),
    SL(1080, "SL");

    private final int resolution;
    private final String label;

    ImageSize(int resolution, String label) {
        this.resolution = resolution;
        this.label = label;
    }

    public int getResolution() {
        return resolution;
    }

    public String getLabel() {
        return label;
    }

    // 🔄 Find by resolution
    public static ImageSize fromResolution(int resolution) {
        for (ImageSize size : values()) {
            if (size.resolution == resolution) {
                return size;
            }
        }
        throw new IllegalArgumentException("Unknown resolution: " + resolution);
    }

    // 🔄 Find by label
    public static ImageSize fromLabel(String label) {
        for (ImageSize size : values()) {
            if (size.label.equalsIgnoreCase(label)) {
                return size;
            }
        }
        throw new IllegalArgumentException("Unknown label: " + label);
    }

    @Override
    public String toString() {
        return resolution + "-" + label;
    }
}
