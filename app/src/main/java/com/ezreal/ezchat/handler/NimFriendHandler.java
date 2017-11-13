package com.ezreal.ezchat.handler;

import android.util.Log;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.FriendServiceObserve;
import com.netease.nimlib.sdk.friend.model.Friend;
import com.netease.nimlib.sdk.friend.model.FriendChangedNotify;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by wudeng on 2017/8/30.
 */

public class NimFriendHandler {
    private static final String TAG = NimFriendHandler.class.getSimpleName();
    private static NimFriendHandler instance;
    private List<String> mFriendAccounts;
    private List<NimUserInfo> mFriendInfos;
    private List<Friend> mFriends;
    private OnFriendUpdateListener mUpdateListener;

    public static NimFriendHandler getInstance() {
        if (instance == null) {
            synchronized (NimFriendHandler.class) {
                if (instance == null) {
                    instance = new NimFriendHandler();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化好友列表工具类
     * 根据账户获取好友列表
     * 同步本地数据库的好友账户数据
     */
    public void init() {
        mFriendAccounts = new ArrayList<>();
        mFriends = new ArrayList<>();
        mFriendInfos = new ArrayList<>();

        // 初始化好友列表更新监听
        NIMClient.getService(FriendServiceObserve.class)
                .observeFriendChangedNotify(new Observer<FriendChangedNotify>() {
                    @Override
                    public void onEvent(FriendChangedNotify notify) {
                        loadFriendData();
                    }
                }, true);
        loadFriendData();
    }

    public List<String> getFriendAccounts() {
        return mFriendAccounts;
    }

    public List<NimUserInfo> getFriendInfos() {
        return mFriendInfos;
    }

    public List<Friend> getFriends() {
        return mFriends;
    }

    /**
     * 读取账户好友列表数据
     */
    private void loadFriendData() {
        mFriendAccounts.clear();
        List<String> friendAccounts = NIMClient.getService(FriendService.class).getFriendAccounts();
        if (friendAccounts == null || friendAccounts.isEmpty()) {
            return;
        }
        mFriendAccounts.addAll(friendAccounts);

        mFriends.clear();
        Friend friend;
        for (String account : mFriendAccounts) {
            friend = NIMClient.getService(FriendService.class).getFriendByAccount(account);
            mFriends.add(friend);
        }

        mFriendInfos.clear();
        List<NimUserInfo> userInfoList = NIMClient.getService(UserService.class)
                .getUserInfoList(mFriendAccounts);
        mFriendInfos.addAll(userInfoList);

        //更新用户界面
        if (mUpdateListener != null){
            mUpdateListener.friendUpdate();
        }
    }

    /**
     * 设置好友列表更新监听
     * @param listener listener
     */
    public void setUpdateListener(OnFriendUpdateListener listener){
        this.mUpdateListener = listener;
    }

    /**
     * 检查该账户是否为我的好友
     *
     * @param account 待检查账户
     * @return true 如果对方是好友，否则返回 false
     */
    public boolean CheckIsMyFriend(String account) {
        return NIMClient.getService(FriendService.class).isMyFriend(account);
    }

    public void syncFriendInfo(List<String> accounts) {
        NIMClient.getService(UserService.class).fetchUserInfo(accounts);
    }

    public interface OnFriendUpdateListener{
        void friendUpdate();
    }
}
