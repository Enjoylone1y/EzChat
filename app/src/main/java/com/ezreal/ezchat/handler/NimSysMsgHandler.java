package com.ezreal.ezchat.handler;

import android.util.Log;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.friend.model.AddFriendNotify;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.model.SystemMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by wudeng on 2017/8/30.
 */

public class NimSysMsgHandler {

    private static final String TAG = NimSysMsgHandler.class.getSimpleName();
    private static NimSysMsgHandler instance;
    private List<SystemMessageListener> mMessageListener;
    public static NimSysMsgHandler getInstance(){
        if (instance == null){
            synchronized (NimSysMsgHandler.class){
                if (instance == null){
                    instance = new NimSysMsgHandler();
                }
            }
        }
        return instance;
    }

    public void init(){
        mMessageListener = new ArrayList<>();
        NIMClient.getService(SystemMessageObserver.class)
                .observeReceiveSystemMsg(new Observer<SystemMessage>() {
            @Override
            public void onEvent(SystemMessage message) {
                AddFriendNotify notify = (AddFriendNotify) message.getAttachObject();
                if (notify != null){
                    for (SystemMessageListener l : mMessageListener){
                        l.addFriendNotify();
                    }
                }
            }
        },true);
    }

    public void setMessageListener(SystemMessageListener listener){
        mMessageListener.add(listener);
    }

    public interface SystemMessageListener{
        void addFriendNotify();
    }
}
