package com.suntek.commonlibrary.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.view.Window;

/**
 *
 * Created by wudeng on 2017/11/6.
 */

public class SystemUtils {

    /**
     * 获取屏幕分辨率高度
     * @param context 上下文
     * @return 屏幕高,与手机分辨率相关
     */
    public static int getScreenHeight(Context context){
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 或者状态栏高度
     * @param context 上下文
     * @return 状态栏高度
     */
    public static int getStatusHeight(Context context){
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height",
                "dimen","android");
        return resources.getDimensionPixelSize(resourceId);
    }

    /**
     * 获取 APP 显示高度
     * @param activity 当前活动状态的 Activity
     * @return AppHeight(include ActionBar) = Screen Height - StatusHeight
     */
    public static int getAppHeight(Activity activity){
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.height();
    }

    /**
     * 获取 View 绘制区域高度
     * @param activity 当前活动状态的 Activity
     * @return ViewDrawingHeight = App Height - ActionBar Height
     */
    public static int getViewDrawingHeight(Activity activity){
        Rect rect = new Rect();
        activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getDrawingRect(rect);
        return rect.height();
    }

    /**
     * 获取软键盘显示高度
     * @param activity 当前活动状态的 Activity
     * @return 软键盘高度 = 分辨率高 - 状态栏高 - 应用可视高,第一次获取,该值为787
     */
    public static int getKeyBoardHeight(Activity activity){
        return getScreenHeight(activity) - getStatusHeight(activity) - getAppHeight(activity);
    }
}
