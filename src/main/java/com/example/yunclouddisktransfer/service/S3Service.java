package com.example.yunclouddisktransfer.service;

import org.springframework.stereotype.Service;
import java.io.File;

@Service
public class S3Service {

    public void downloadFile(String downloadUrl, String destPath) {
        // 通过HTTP下载文件
        java.io.InputStream in = null;
        java.io.FileOutputStream out = null;
        try {
            java.net.URL url = new java.net.URL(downloadUrl);
            in = url.openStream();
            out = new java.io.FileOutputStream(destPath);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (in != null) in.close(); } catch (Exception e) {}
            try { if (out != null) out.close(); } catch (Exception e) {}
        }
    }

    public void uploadFile(String uploadUrl, File file, String contentType) {
        // 通过HTTP PUT上传文件
        java.io.OutputStream out = null;
        java.io.FileInputStream in = null;
        java.net.HttpURLConnection conn = null;
        try {
            java.net.URL url = new java.net.URL(uploadUrl);
            conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", contentType);
            out = conn.getOutputStream();
            in = new java.io.FileInputStream(file);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
            int responseCode = conn.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                throw new RuntimeException("Upload failed, HTTP code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getCause());
        } finally {
            try { if (in != null) in.close(); } catch (Exception e) {}
            try { if (out != null) out.close(); } catch (Exception e) {}
            if (conn != null) conn.disconnect();
        }
    }
} 