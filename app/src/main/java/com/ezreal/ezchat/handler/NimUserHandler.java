package com.ezreal.ezchat.handler;

import android.util.Log;

import com.ezreal.ezchat.bean.LocalAccountBean;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.UserServiceObserve;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.suntek.commonlibrary.utils.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wudeng on 2017/8/30.
 */

public class NimUserHandler {

    private static final String TAG = NimUserHandler.class.getSimpleName();
    private static NimUserHandler instance;
    private String mMyAccount;
    private NimUserInfo mUserInfo;
    private LocalAccountBean mLocalAccount;
    private List<OnInfoUpdateListener> mUpdateListeners;

    public static NimUserHandler getInstance(){
        if (instance == null){
            synchronized (NimUserHandler.class){
                if (instance == null){
                    instance = new NimUserHandler();
                }
            }
        }
        return instance;
    }

    public void init(){
        mUserInfo = NIMClient.getService(UserService.class).getUserInfo(mMyAccount);
        mUpdateListeners = new ArrayList<>();
        // 初始化监听
        NIMClient.getService(UserServiceObserve.class)
                .observeUserInfoUpdate(new Observer<List<NimUserInfo>>() {
            @Override
            public void onEvent(List<NimUserInfo> userInfoList) {
                NimUserInfo userInfo = userInfoList.get(0);
                Log.e(TAG,"UserInfoUpdate"+userInfo.toString());
            }
        },true);
    }

    /**
     * 从服务器账户数据到本地数据库
     */
    public void fetchAccountInfo(){
        List<String> accounts = new ArrayList<>();
        accounts.add(mMyAccount);
        NIMClient.getService(UserService.class).fetchUserInfo(accounts)
                .setCallback(new RequestCallback<List<NimUserInfo>>() {
            @Override
            public void onSuccess(List<NimUserInfo> param) {
                Log.e(TAG,"fetchAccountInfo onSuccess ");
                mUserInfo = param.get(0);
                // 同步成功，通知刷新
                for (OnInfoUpdateListener listener : mUpdateListeners){
                    listener.myInfoUpdate();
                }
            }

            @Override
            public void onFailed(int code) {
                Log.e(TAG,"fetchAccountInfo onFailed code " + code);
            }

            @Override
            public void onException(Throwable exception) {
                Log.e(TAG,"fetchAccountInfo onException message " + exception.getMessage());
            }
        });
    }

    public void syncChange2Service(){
        Map<UserInfoFieldEnum,Object> fields = new HashMap<>();
        if(!TextUtils.isEmpty(mLocalAccount.getHeadImgUrl())){
            fields.put(UserInfoFieldEnum.AVATAR,mLocalAccount.getHeadImgUrl());
        }
        if (!TextUtils.isEmpty(mLocalAccount.getBirthDay())){
            fields.put(UserInfoFieldEnum.BIRTHDAY,mLocalAccount.getBirthDay());
        }
        if (!TextUtils.isEmpty(mLocalAccount.getLocation())){
            fields.put(UserInfoFieldEnum.EXTEND,mLocalAccount.getLocation());
        }
        if (!TextUtils.isEmpty(mLocalAccount.getSignature())){
            fields.put(UserInfoFieldEnum.SIGNATURE,mLocalAccount.getSignature());
        }

        fields.put(UserInfoFieldEnum.Name,mLocalAccount.getNick());
        fields.put(UserInfoFieldEnum.GENDER,mLocalAccount.getGenderEnum().getValue());
        NIMClient.getService(UserService.class).updateUserInfo(fields)
                .setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                Log.e(TAG,"syncChange2Service onSuccess");
                // 上传成功，更新本地数据库
                fetchAccountInfo();
            }

            @Override
            public void onFailed(int code) {
                Log.e(TAG,"syncChange2Service onFailed code " + code);
                //TODO 同步失败应当后台服务上传
            }

            @Override
            public void onException(Throwable exception) {
                Log.e(TAG,"syncChange2Service onException message " + exception.getMessage());
            }
        });
    }

    public LocalAccountBean getLocalAccount() {
        mLocalAccount = new LocalAccountBean();
        mLocalAccount.setAccount(mUserInfo.getAccount());
        mLocalAccount.setHeadImgUrl(mUserInfo.getAvatar());
        mLocalAccount.setBirthDay(mUserInfo.getBirthday());
        mLocalAccount.setNick(mUserInfo.getName());
        mLocalAccount.setSignature(mUserInfo.getSignature());
        mLocalAccount.setGenderEnum(mUserInfo.getGenderEnum());
        String extension = mUserInfo.getExtension();
        if (!TextUtils.isEmpty(extension)){
            mLocalAccount.setLocation(extension);
        }
        return mLocalAccount;
    }


    public void setLocalAccount(LocalAccountBean account){
        this.mLocalAccount = account;
    }

    public String getMyAccount() {
        return mMyAccount;
    }

    public void setMyAccount(String account) {
        mMyAccount = account;
    }

    public NimUserInfo getUserInfo() {
        return mUserInfo;
    }

    public void setUpdateListeners(OnInfoUpdateListener listeners){
        mUpdateListeners.add(listeners);
    }

    public interface OnInfoUpdateListener{
        void myInfoUpdate();
    }
}
