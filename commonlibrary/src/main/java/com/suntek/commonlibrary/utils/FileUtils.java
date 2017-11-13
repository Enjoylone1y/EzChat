package com.suntek.commonlibrary.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 文件工具类
 * Created by wudeng on 2017/7/24.
 */

public class FileUtils {

    /**
     * 递归创建文件夹，从最上层文件夹开始，只要不存在就会新建
     * @param dirPath 文件夹完整路径
     */
    public static void mkDir(String dirPath) {
        String[] dirArray = dirPath.split("/");
        String pathTemp = "";
        for (int i = 1; i < dirArray.length; i++) {
            pathTemp = pathTemp + "/" + dirArray[i];
            File newF = new File(dirArray[0] + pathTemp);
            if (!newF.exists()) {
                newF.mkdir();
            }
        }
    }

    /**
     * 文件转 byte 数组
     * @param file 待转换文件
     * @return byte[]
     * @throws IOException 转换出错
     */
    public static byte[] file2byte(File file) throws IOException {
        byte[] bytes = null;
        if (file != null) {
            InputStream is = new FileInputStream(file);
            int length = (int) file.length();
            if (length >= Integer.MAX_VALUE) {
                Log.e("FileUtils","this file is max ");
                is.close();
                return null;
            }
            bytes = new byte[length];
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
            is.close();
            if (offset < bytes.length) {
                Log.e("FileUtils","file length is error");
                return null;
            }
        }
        return bytes;
    }

    /**
     * 从Uri获取媒体文件，如图片或音频文件
     * @param context 上下文
     * @param uri 从 content provider 或者 onActivityResult 返回的 Uri
     * @return Uri 的媒体文件
     */
    public static File getFileFromMediaUri(Context context, Uri uri) {
        if (uri.getScheme().compareTo("content") == 0) {
            ContentResolver cr = context.getContentResolver();
            Cursor cursor = cr.query(uri, null, null, null, null);// 根据Uri从数据库中找
            if (cursor != null) {
                cursor.moveToFirst();
                String filePath = cursor.getString(cursor.getColumnIndex("_data"));// 获取图片路径
                cursor.close();
                if (filePath != null) {
                    return new File(filePath);
                }
            }
        } else if (uri.getScheme().compareTo("file") == 0) {
            return new File(uri.toString().replace("file://", ""));
        }
        return null;
    }

    /**
     * 解压文件或文件夹到指定路径
     *
     * @param zipFilePath  待解压文件路径
     * @param outPath      输出根目录路径
     * @throws Exception 解压出错
     */
    public static void unZipFolder(String zipFilePath, String outPath) throws Exception {
        ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry zipEntry;
        String szName;
        while ((zipEntry = inZip.getNextEntry()) != null) {
            szName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(outPath + File.separator + szName);
                folder.mkdirs();
            } else {
                File file = new File(outPath + File.separator + szName);
                file.createNewFile();
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[1024];
                while ((len = inZip.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                    out.flush();
                }
                out.close();
            }
        }
        inZip.close();
    }
}
