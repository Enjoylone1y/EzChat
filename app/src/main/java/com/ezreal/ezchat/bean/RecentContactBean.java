package com.ezreal.ezchat.bean;

import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;

/**
 * Created by wudeng on 2017/9/11.
 */

public class RecentContactBean {
    private RecentContact mRecentContact;
    private UserInfo mUserInfo;

    public RecentContact getRecentContact() {
        return mRecentContact;
    }

    public void setRecentContact(RecentContact recentContact) {
        mRecentContact = recentContact;
    }

    public UserInfo getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        mUserInfo = userInfo;
    }
}
