package com.ezreal.photoselector;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 读取SD卡图片列表工具类
 * Created by wudeng on 2017/11/7.
 */

public class ImageUtils {

    public static void loadImageList(final Context context, final OnLoadImageCallBack callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver resolver = context.getContentResolver();
                Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Images.ImageColumns._ID,
                                MediaStore.Images.ImageColumns.DATA,
                                MediaStore.Images.ImageColumns.DISPLAY_NAME,
                                MediaStore.Images.ImageColumns.SIZE,
                                MediaStore.Images.ImageColumns.DATE_ADDED},
                        null, null, MediaStore.Images.ImageColumns.DATE_ADDED + " desc");
                List<ImageBean> imageBeans = new ArrayList<>();
                if (cursor != null) {
                    ImageBean bean;
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID));
                        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                        String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
                        long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.SIZE));
                        long date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_ADDED));
                        bean = new ImageBean(id, name, path, size, date);
                        imageBeans.add(bean);
                    }
                    cursor.close();
                }
                callBack.callBack(splitImage2Folder(imageBeans));
            }
        }).start();
    }

    private static List<FolderBean> splitImage2Folder(List<ImageBean> imageList) {
        List<FolderBean> folderList = new ArrayList<>();
        if (!imageList.isEmpty()) {

            // 生成全部图片文件夹
            FolderBean allFolder = new FolderBean("全部图片");
            for (ImageBean bean : imageList){
                if (bean.getSize() > 50 * 1024){
                    allFolder.addImage(bean);
                }
            }
            folderList.add(allFolder);

            // 根据图片路径划分文件夹
            for (int i = 0; i < imageList.size(); i++) {
                String path = imageList.get(i).getPath();
                String name = getFolderName(path);
                if (!TextUtils.isEmpty(name)) {
                    FolderBean folder = getFolder(name, folderList);
                    folder.addImage(imageList.get(i));
                }
            }
        }

        return folderList;
    }

    private static String getFolderName(String path) {
        if (!TextUtils.isEmpty(path)) {
            String[] strings = path.split(File.separator);
            if (strings.length >= 2) {
                return strings[strings.length - 2];
            }
        }
        return "";
    }

    private static FolderBean getFolder(String name, List<FolderBean> folders) {
        for (int i = 0; i < folders.size(); i++) {
            FolderBean folder = folders.get(i);
            if (name.equals(folder.getName())) {
                return folder;
            }
        }
        FolderBean newFolder = new FolderBean(name);
        folders.add(newFolder);
        return newFolder;
    }

    public interface OnLoadImageCallBack {
        void callBack(List<FolderBean> folderList);
    }

}
