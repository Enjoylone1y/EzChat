package com.ezreal.ezchat.http;


import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ezreal.ezchat.ChatApplication;
import com.ezreal.ezchat.utils.CheckSumUtils;
import com.ezreal.ezchat.utils.Constant;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


/**
 * Created by wudeng on 2017/8/24.
 */

public class NimClientHandle {

    private static final String TAG = NimClientHandle.class.getSimpleName();

    private String APP_SERVER_BASE_URL = "https://api.netease.im/nimserver/";

    private String mAppServerUserCreate = "user/create.action";
    private String mAppServerUserUpdate = "user/update.action";
    private String mAppServerTokenUpdate = "user/refreshToken.action";
    private String mAppServerUserInfo = "user/getUinfos.action";
    private String mAppServerUserInfoUpdate = "user/updateUinfo.action";

    private static NimClientHandle instance;
    private OkHttpClient mOkHttpClient;

    public static NimClientHandle getInstance() {
        if (instance == null) {
            synchronized (NimClientHandle.class) {
                if (instance == null) {
                    instance = new NimClientHandle();
                }
            }
        }
        return instance;
    }

    private NimClientHandle() {
        initApi();
    }

    private void initApi() {
        mOkHttpClient = new OkHttpClient();
    }

    public void register(String account, String token, String name, final OnRegisterListener listener) {

        final RequestBody body = new FormBody.Builder()
                .add("accid", account)
                .add("token", token)
                .add("name", name)
                .build();

        Request request = new Request.Builder()
                .url(APP_SERVER_BASE_URL + mAppServerUserCreate)
                .headers(createHeaders())
                .post(body)
                .build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                listener.onFailed(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if (listener != null) {
                    if (response.code() == 200) {
                        Gson gson = new Gson();
                        RegisterResp resp = gson.fromJson(response.body().string(),
                                new TypeToken<RegisterResp>(){}.getType());
                        if (resp == null){
                            listener.onFailed("解析返回数据失败" + response.body().string());
                            return;
                        }
                        if (resp.getCode() != 200){
                            listener.onFailed(resp.getDesc());
                            return;
                        }
                        listener.onSuccess();
                    } else {
                        listener.onFailed(response.message());
                    }
                }
            }
        });
    }

    public void updateToken(String account,String pass,final OnRegisterListener listener){
        final RequestBody body = new FormBody.Builder()
                .add("accid", account)
                .add("token", pass)
                .build();

        Request request = new Request.Builder()
                .url(APP_SERVER_BASE_URL + mAppServerUserUpdate)
                .headers(createHeaders())
                .post(body)
                .build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                listener.onFailed(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (listener != null) {
                    if (response.code() == 200) {
                        Gson gson = new Gson();
                        RegisterResp resp = gson.fromJson(response.body().string(),
                                new TypeToken<RegisterResp>(){}.getType());
                        if (resp == null){
                            listener.onFailed("解析返回数据失败" + response.body().string());
                            return;
                        }
                        if (resp.getCode() != 200){
                            listener.onFailed(resp.getDesc());
                            return;
                        }
                        listener.onSuccess();
                    } else {
                        listener.onFailed(response.message());
                    }
                }
            }
        });
    }


    /**
     * 生成访问 NIM  APP-SERVICE 所要求的 HEADER
     *
     * @return headers ,in OK HTTP3
     */
    private Headers createHeaders() {
        String nonce = CheckSumUtils.getNonce();
        String time = String.valueOf(System.currentTimeMillis() / 1000L);
        return new Headers.Builder()
                .add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .add("AppKey", readAppKey())
                .add("Nonce", nonce)
                .add("CurTime", time)
                .add("CheckSum", CheckSumUtils.getCheckSum(Constant.APP_SECURY, nonce, time))
                .build();
    }

    /**
     * 读取存储于manifest文件下的 APP KEY
     *
     * @return APP key
     */
    private String readAppKey() {
        try {
            ApplicationInfo appInfo = ChatApplication.getInstance().getPackageManager()
                    .getApplicationInfo(ChatApplication.getInstance().getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo != null) {
                return appInfo.metaData.getString("com.netease.nim.appKey");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }
}
