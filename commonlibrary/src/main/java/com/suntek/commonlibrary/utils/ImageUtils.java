package com.suntek.commonlibrary.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.IntegerRes;
import android.support.annotation.StringRes;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Image utils
 * Created by wudeng on 2017/7/25.
 */


public class ImageUtils {

    /**
     * 从文件获取 bitmap ，并根据给定的显示宽高对 bitmap 进行缩放
     *
     * @param filePath 文件路径
     * @param height   需要显示的高度
     * @param width    需要显示的宽度
     * @return 缩放后的 bitmap，若获取失败，返回 null
     */
    public static Bitmap getBitmapFromFile(String filePath, int height, int width) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;
        if ((srcWidth == -1) || (srcHeight == -1))
            return null;
        int inSampleSize = 1;
        if (srcHeight > height || srcWidth > width) {
            if (srcWidth > srcHeight) {
                inSampleSize = Math.round(srcHeight / height);
            } else {
                inSampleSize = Math.round(srcWidth / width);
            }
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;

        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 从 Uri 获取 Bitmap，并根据给定的显示高度和宽度，对 bitmap 进行缩放
     *
     * @param context 上下文
     * @param uri     指向 bitmap 资源文件的 Uri
     * @param height  需要显示的高度
     * @param width   需要显示的宽度
     * @return 缩放后的 bitmap ，若 Uri 指定的目标读取不到 bitmap 则返回 null
     * @throws FileNotFoundException 未找到 uri 所指向的文件
     */
    public static Bitmap getBitmapFormUri(Context context, Uri uri, int height, int width) throws FileNotFoundException {
        InputStream input = context.getContentResolver().openInputStream(uri);
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1))
            return null;
        int scale = 1;
        if (originalWidth > originalHeight && originalWidth > width) {
            scale = (originalWidth / width);
        } else if (originalWidth < originalHeight && originalHeight > height) {
            scale = (originalHeight / height);
        }
        if (scale <= 0) {
            scale = 1;
        }
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = scale;
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);

        return compressImageBySize(bitmap, 500);
    }

    /**
     * 根据给定的目标大小对 bitmap 进行压缩
     *
     * @param bitmap 原始 bitmap
     * @param size   目标大小,单位 kb
     * @return 压缩后的 bitmap
     */
    public static Bitmap compressImageBySize(Bitmap bitmap, int size) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        while (baos.toByteArray().length / 1024 > size) {
            baos.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());

        return BitmapFactory.decodeStream(isBm, null, null);
    }

    /**
     * 获取图片旋转角度
     *
     * @param path 图片路径
     * @return 旋转角度
     */
    public static int getBitmapDegree(String path) {
        int degree = 0;
        try {
          /*
            TAG_DATETIME时间日期
            TAG_FLASH闪光灯
            TAG_GPS_LATITUDE纬度
            TAG_GPS_LATITUDE_REF纬度参考
            TAG_GPS_LONGITUDE经度
            TAG_GPS_LONGITUDE_REF经度参考
            TAG_IMAGE_LENGTH图片长
            TAG_IMAGE_WIDTH图片宽
            TAG_MAKE设备制造商
            TAG_MODEL设备型号
            TAG_ORIENTATION方向
            TAG_WHITE_BALANCE白平衡 */

            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 根据给定的角度，对 bitmap 进行旋转
     *
     * @param bitmap 原始 bitmap
     * @param degree 旋转角度
     * @return 旋转之后的 bitmap
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bitmap, int degree) {
        Bitmap returnBm;
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            returnBm = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            Log.e("ImageUtils", e.getMessage());
            return bitmap;
        }
        if (returnBm == null) {
            returnBm = bitmap;
        }
        if (bitmap != returnBm) {
            bitmap.recycle();
        }
        return returnBm;
    }


    /**
     * compress bitmap to byte[]
     *
     * @param bitmap  bitmap
     * @param quality compress quality
     * @return byte[]
     */
    public static byte[] bitmap2Bytes(Bitmap bitmap, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        return baos.toByteArray();
    }

    /**
     * Bitmap 转 InputStream JPEG 格式
     *
     * @param bitmap  bitmap
     * @param quality 图片质量
     * @return is
     */
    public static InputStream bitmap2InputStream(Bitmap bitmap, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * 将Bitmap转换成Base64字符串
     *
     * @param bit 图片
     * @return base64 编码的图片
     */
    public static String Bitmap2StrByBase64(Bitmap bit) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, 40, bos);//参数100表示不压缩
        byte[] bytes = bos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * 将base64编码的字符串转换成bitmap
     *
     * @param st base64编码的字符串
     * @return bitmap
     */
    public static Bitmap base64Str2Bitmap(String st) {
        Bitmap bitmap;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(st, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
                    bitmapArray.length);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getFilePathFromUri(Context context,Uri uri) {
        String filePath = "";
        String[] filePathColumn = {MediaStore.MediaColumns.DATA};
        Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
        if (cursor != null){
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        }
        return filePath;
    }

    /**
     * 将 bitmap 保存到本地 jpeg
     *
     * @param bitmap 图片 bitmap
     * @param path   保存路径（全路径）
     * @throws IOException
     */
    public static void saveBitmap2Jpg(Bitmap bitmap, String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        OutputStream outputStream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
    }

    /**
     * load bitmap by url into imageView with diskCache
     */
    public static void setImageByUrl(Context context, ImageView imageView, String url,int default_img) {
        setImageByString(context, imageView, url, default_img);
    }

    /**
     * load bitmap by file path into imageView with diskCache
     */
    public static void setImageByFile(Context context, ImageView imageView, String filePath,int default_img) {
        setImageByString(context, imageView, filePath, default_img);
    }

    /**
     * load bitmap by uri into imageView with diskCache
     */
    public static void setImageByUri(Context context, ImageView imageView, String uri,int default_img) {
        setImageByString(context, imageView, uri, default_img);
    }


    private static void setImageByString(Context context, ImageView imageView, String path,int default_img) {
        Glide.with(context)
                .load(path)
                .asBitmap()
                .error(default_img)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中

        return BitmapFactory.decodeStream(isBm, null, null);
    }

}
