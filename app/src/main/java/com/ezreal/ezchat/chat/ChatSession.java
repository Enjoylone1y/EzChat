package com.ezreal.ezchat.chat;

import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

/**
 * Created by wudeng on 2017/9/13.
 */

public class ChatSession {

    private SessionTypeEnum mSessionType;
    private String mSessionId;
    private String mMyAccount;
    private String mChatAccount;
    private String mChatNick;
    private NimUserInfo mMyInfo;
    private NimUserInfo mChatInfo;

    public SessionTypeEnum getSessionType() {
        return mSessionType;
    }

    public void setSessionType(SessionTypeEnum sessionType) {
        mSessionType = sessionType;
    }

    public String getSessionId() {
        return mSessionId;
    }

    public void setSessionId(String sessionId) {
        mSessionId = sessionId;
    }

    public String getMyAccount() {
        return mMyAccount;
    }

    public void setMyAccount(String myAccount) {
        mMyAccount = myAccount;
    }

    public String getChatAccount() {
        return mChatAccount;
    }


    public void setChatAccount(String chatAccount) {
        mChatAccount = chatAccount;
    }

    public String getChatNick() {
        return mChatNick;
    }

    public void setChatNick(String chatNick) {
        mChatNick = chatNick;
    }

    public NimUserInfo getMyInfo() {
        return mMyInfo;
    }

    public void setMyInfo(NimUserInfo myInfo) {
        mMyInfo = myInfo;
    }

    public NimUserInfo getChatInfo() {
        return mChatInfo;
    }

    public void setChatInfo(NimUserInfo chatInfo) {
        mChatInfo = chatInfo;
    }
}
