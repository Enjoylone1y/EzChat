package com.ezreal.ezchat.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.LruCache;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.attachment.VideoAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.suntek.commonlibrary.utils.ImageUtils;
import com.suntek.commonlibrary.utils.TextUtils;

/**
 * Created by wudeng on 2017/11/1.
 */

public class ChatUtils {

    private Context mContext;
    private LruCache<String, Bitmap> mLruCache;

    public ChatUtils(Context context){
        mContext = context;

        //获取应用最大可用内存，取1/8作为缓存内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory / 8;
        //初始化图片缓存
        mLruCache = new LruCache<String, Bitmap>(cacheMemory){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //返回图片所占内存
                return value.getRowBytes() * value.getHeight();
            }
        };

    }


    public  boolean isTransferring(IMMessage message) {
        return message.getStatus() == MsgStatusEnum.sending || (message.getAttachment() != null
                && message.getAttachStatus() == AttachStatusEnum.transferring);
    }

    public Bitmap getBitmap(ImageAttachment attachment) {

        // 优先显示缩略图，但是要限制宽高
        if (!TextUtils.isEmpty(attachment.getThumbPath())) {
            Bitmap bitmap = mLruCache.get(attachment.getThumbPath());
            if (bitmap == null){
                bitmap = ImageUtils.getBitmapFromFile(attachment.getThumbPath(), 400, 180);
                mLruCache.put(attachment.getThumbPath(),bitmap);
            }

            return bitmap;
        }

        // 缩略图不存在的情况下显示原图，但是要限制宽高
        if (!TextUtils.isEmpty(attachment.getPath())) {
            Bitmap bitmap = mLruCache.get(attachment.getPath());
            if (bitmap == null){
                bitmap = ImageUtils.getBitmapFromFile(attachment.getPath(), 400, 180);
                mLruCache.put(attachment.getPath(),bitmap);
            }

            return bitmap;
        }

        return null;
    }


    public String getAudioTime(long duration) {
        return String.valueOf(duration / 1000.0) + "‘";
    }


    public void setAudioLayoutWidth(RelativeLayout layout, long duration) {

        float perSecondWidth = 4.0f;
        float second = duration / 1000.0f;
        float width = second * perSecondWidth;

        if (width < 60){
            width = 60.0f;
        }else if (width > 240){
            width = 240.0f;
        }

        int dpWidth = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,width,
                mContext.getResources().getDisplayMetrics()));

        ViewGroup.LayoutParams params = layout.getLayoutParams();
        params.width = dpWidth;
        layout.setLayoutParams(params);
    }


    public  Bitmap getVideoCover( VideoAttachment attachment) {

        if (!TextUtils.isEmpty(attachment.getThumbPath())) {
            Bitmap bitmap = mLruCache.get(attachment.getThumbPath());
            if (bitmap == null){
                bitmap = ImageUtils.getBitmapFromFile(attachment.getThumbPath(), 400, 180);
                mLruCache.put(attachment.getThumbPath(),bitmap);
            }
            return bitmap;
        }

        if (!TextUtils.isEmpty(attachment.getPath())) {
            Bitmap bitmap = mLruCache.get(attachment.getPath());
            if (bitmap == null){
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(attachment.getPath());
                bitmap = retriever.getFrameAtTime();
                mLruCache.put(attachment.getPath(),bitmap);
            }
            return bitmap;
        }

        return null;
    }

    public  String getVideoTime(long duration) {
        // second  最大10秒
        int second = (int) (duration / 1000);
        if (second < 10){
            return "0:0" + String.valueOf(second);
        }else {
            return "0:10";
        }
    }

}
