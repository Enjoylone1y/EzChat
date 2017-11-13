package com.suntek.commonlibrary.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Assets 文件工具类
 * Created by wudeng on 2017/7/25.
 */

public class AssetsUtils {

    /**
     * 将 Assets 文件输出到指定路径
     * @param context 上下文
     * @param fileName 待输出文件文件名
     * @param outPath 文件输出路径（不包含文件名）
     * @throws IOException 输出出错
     */
    public static void assetsDataToStorage(Context context,
                                            String fileName,String outPath) throws IOException {
        String fullPath = outPath + File.separator + fileName;

        OutputStream outputStream = new FileOutputStream(fullPath);
        InputStream inputStream = context.getAssets().open(fileName);
        byte[] buffer = new byte[1024];
        int length = inputStream.read(buffer);
        while (length > 0) {
            outputStream.write(buffer, 0, length);
            length = inputStream.read(buffer);
        }
        outputStream.flush();
        inputStream.close();
        outputStream.close();
    }


}
