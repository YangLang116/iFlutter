package com.xtu.plugin.flutter.utils;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;

public class FileUtils {

    public static boolean isChildFile(String childFilePath, String parentDirName) {
        return childFilePath.contains(File.separator + parentDirName + File.separator);
    }

    public static String getRelativePath(String parentPath, String childFilePath) {
        if (StringUtils.isEmpty(parentPath)) return childFilePath;
        if (!parentPath.endsWith(File.separator)) {
            parentPath += File.separator;
        }
        if (!childFilePath.startsWith(parentPath)) return null;
        return childFilePath.substring(parentPath.length());
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
