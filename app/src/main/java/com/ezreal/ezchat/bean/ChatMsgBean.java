package com.ezreal.ezchat.bean;

import com.netease.nimlib.sdk.msg.model.IMMessage;

/**
 * Created by wudeng on 2017/9/5.
 */

public class ChatMsgBean {
    private int type;
    private String anchor;
    private IMMessage mMessage;
    public String getAnchor() {
        return anchor;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    public IMMessage getMessage() {
        return mMessage;
    }

    public void setMessage(IMMessage message) {
        mMessage = message;
    }
}
