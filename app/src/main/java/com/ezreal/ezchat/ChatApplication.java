package com.ezreal.ezchat;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Process;
import android.os.StrictMode;

import android.text.TextUtils;

import androidx.multidex.MultiDex;

import com.ezreal.ezchat.activity.MainActivity;
import com.ezreal.ezchat.handler.NimUserHandler;
import com.ezreal.ezchat.utils.Constant;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.suntek.commonlibrary.utils.FileUtils;
import com.suntek.commonlibrary.utils.SharedPreferencesUtil;

import java.io.File;
import java.util.Stack;

/**
 * Created by wudeng on 2017/8/22.
 */

public class ChatApplication extends Application {

    private static ChatApplication instance;
    private static Stack<Activity> sActivityStack;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // 解决7.0 以上系统照相机启动失败导致 APP 崩溃问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        sActivityStack = new Stack<>();

        // 初始化网易云通讯 SDK
        NIMClient.init(this,getLoginInfo(),getOptions());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private SDKOptions getOptions(){
        SDKOptions options = new SDKOptions();

        // 该目录目前包含 log, file, image, audio, video, thumb 这6个目录。
        // 如果第三方 APP 需要缓存清理功能， 清理这个目录下面个子目录的内容即可。
        if (!new File(Constant.APP_CACHE_PATH).exists()){
            FileUtils.mkDir(Constant.APP_CACHE_PATH);
        }
        options.sdkStorageRootPath = Constant.APP_CACHE_PATH;


        // 用户资料提供者, 目前主要用于提供用户资料，用于新消息通知栏中显示消息来源的头像和昵称
        options.userInfoProvider = new UserInfoProvider() {
            @Override
            public UserInfo getUserInfo(String account) {
                return NIMClient.getService(UserService.class).getUserInfo(account);
            }

            @Override
            public String getDisplayNameForMessageNotifier(String account,
                                                           String sessionId, SessionTypeEnum sessionType) {
                return account;
            }

            @Override
            public Bitmap getAvatarForMessageNotifier(SessionTypeEnum sessionType, String sessionId) {
                return BitmapFactory.decodeResource(getResources(),R.mipmap.app_logo);
            }
        };

        // 配置是否需要预下载附件缩略图，默认为 true
        options.preloadAttach = true;
        StatusBarNotificationConfig config = new StatusBarNotificationConfig();
        config.ring = false;
        config.notificationEntrance = MainActivity.class;
        config.vibrate = true;

        options.statusBarNotificationConfig = config;
        return options;
    }

    private LoginInfo getLoginInfo(){
        String account = SharedPreferencesUtil.getStringSharedPreferences(this,
                Constant.LOCAL_LOGIN_TABLE, Constant.LOCAL_USER_ACCOUNT);
        String token = SharedPreferencesUtil.getStringSharedPreferences(this,
                Constant.LOCAL_LOGIN_TABLE, Constant.LOCAL_USER_TOKEN);
        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)){
            LoginInfo info = new LoginInfo(account,token);
            NimUserHandler.getInstance().setMyAccount(account);
            return info;
        }
        return null;
    }

    public static ChatApplication getInstance(){
        return instance;
    }

    public void addActivity(Activity activity){
        sActivityStack.add(activity);
    }

    public void finishActivity(Activity activity){
        sActivityStack.remove(activity);
        activity.finish();
    }

    public void finishAllActivity(){
        for (int i=0;i<sActivityStack.size();i++){
            if (sActivityStack.get(i) != null){
                sActivityStack.get(i).finish();
            }
        }
    }

    public void AppExit(){
        try {
            finishAllActivity();
            Process.killProcess(Process.myPid());
            System.exit(0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
