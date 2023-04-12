package com.xtu.plugin.flutter.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public class FileUtils {

    @NotNull
    public static String getFileName(@NotNull File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) return fileName;
        return fileName.substring(0, dotIndex);
    }

    @Nullable
    public static String getExtension(@NotNull File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex >= 0 ? fileName.substring(dotIndex + 1) : null;
    }

    public static String getMd5(@NotNull File file) {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            return DigestUtils.md5Hex(inputStream);
        } catch (Exception e) {
            return null;
        } finally {
            CloseUtils.close(inputStream);
        }
    }

    @Nullable
    public static String getRelativePath(@NotNull String parentPath, File childFile) {
        File parentFile = new File(parentPath); //兼容windows
        String parentAbsolutePath = parentFile.getAbsolutePath();
        String childAbsolutePath = childFile.getAbsolutePath();
        if (!childAbsolutePath.startsWith(parentAbsolutePath)) return null;
        String relativePath = childAbsolutePath.substring(parentAbsolutePath.length());
        if (relativePath.startsWith(File.separator)) {
            relativePath = relativePath.substring(1);
        }
        return relativePath.replace(File.separator, "/");
    }

    public static boolean write2File(File file, String content) {
        if (file == null) return false;
        FileOutputStream fileOutputStream = null;
        try {
            if (!file.exists()) {
                boolean ignore = file.createNewFile();
            }
            if (!file.canWrite()) return false;
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(content.getBytes("UTF-8"));
            fileOutputStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            CloseUtils.close(fileOutputStream);
        }
    }

    public static String readFromFile(File file) throws Exception {
        if (!file.exists() || !file.canRead()) return null;
        FileInputStream fileInputStream = null;
        ByteArrayOutputStream outputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            outputStream = new ByteArrayOutputStream();
            int len = -1;
            byte[] buffer = new byte[1024];
            while ((len = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            return outputStream.toString();
        } finally {
            CloseUtils.close(fileInputStream);
            CloseUtils.close(outputStream);
        }
    }

    public static void scanDirectory(File directory, OnScanFileListener scanFileListener) {
        if (directory == null || !directory.isDirectory()) return;
        File[] listFiles = directory.listFiles();
        if (ArrayUtils.isEmpty(listFiles)) return;
        for (File file : listFiles) {
            if (file.isDirectory()) {
                scanDirectory(file, scanFileListener);
            } else {
                scanFileListener.onGetFile(file);
            }
        }
    }

    public interface OnScanFileListener {

        void onGetFile(File file);

    }
}
