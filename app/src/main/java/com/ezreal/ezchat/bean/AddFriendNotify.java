package com.ezreal.ezchat.bean;

import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;


/**
 * Created by wudeng on 2017/8/30.
 */

public class AddFriendNotify {
    private SystemMessage mMessage;
    private NimUserInfo mUserInfo;
    private boolean isMyFriend;

    public SystemMessage getMessage() {
        return mMessage;
    }

    public void setMessage(SystemMessage message) {
        mMessage = message;
    }

    public NimUserInfo getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(NimUserInfo userInfo) {
        mUserInfo = userInfo;
    }

    public boolean isMyFriend() {
        return isMyFriend;
    }

    public void setMyFriend(boolean myFriend) {
        isMyFriend = myFriend;
    }
}
