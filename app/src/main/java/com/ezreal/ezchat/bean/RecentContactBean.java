package com.ezreal.ezchat.bean;

import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;

/**
 * Created by wudeng on 2017/9/11.
 */

public class RecentContactBean {
    private RecentContact mRecentContact;
    private UserInfoProvider.UserInfo mUserInfo;

    public RecentContact getRecentContact() {
        return mRecentContact;
    }

    public void setRecentContact(RecentContact recentContact) {
        mRecentContact = recentContact;
    }

    public UserInfoProvider.UserInfo getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(UserInfoProvider.UserInfo userInfo) {
        mUserInfo = userInfo;
    }
}
