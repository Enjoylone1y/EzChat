package com.ezreal.ezchat.handler;

import android.util.Log;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wudeng on 2017/8/31.
 */

public class NimOnlineStatusHandler {

    private static final String TAG = NimOnlineStatusHandler.class.getSimpleName();
    private static NimOnlineStatusHandler instance;
    private List<OnStatusChangeListener> mListeners;

    public static NimOnlineStatusHandler getInstance(){
        if (instance == null){
            synchronized (NimOnlineStatusHandler.class){
                if (instance == null){
                    instance = new NimOnlineStatusHandler();
                }
            }
        }

        return instance;
    }

    public void init(){
        mListeners = new ArrayList<>();
        NIMClient.getService(AuthServiceObserver.class)
                .observeOnlineStatus(new Observer<StatusCode>() {
            @Override
            public void onEvent(StatusCode statusCode) {
                if (statusCode == StatusCode.UNLOGIN || statusCode == StatusCode.FORBIDDEN){
                    Log.e(TAG,"OnlineObserver---UN_LOGIN");
                    if (mListeners != null && !mListeners.isEmpty()){
                        for (OnStatusChangeListener listener : mListeners){
                            listener.requestReLogin("UN_LOGIN");
                        }
                    }
                }else if (statusCode == StatusCode.KICK_BY_OTHER_CLIENT
                        || statusCode == StatusCode.KICKOUT){
                    Log.e(TAG,"OnlineObserver---KICK_OUT");
                    if (mListeners != null && !mListeners.isEmpty()){
                        for (OnStatusChangeListener listener : mListeners){
                            listener.requestReLogin("KICK_OUT");
                        }
                    }
                } else if (statusCode == StatusCode.NET_BROKEN){
                    Log.e(TAG,"OnlineObserver---NET_BROKEN");
                    if (mListeners != null && !mListeners.isEmpty()){
                        for (OnStatusChangeListener listener : mListeners){
                            listener.networkBroken();
                        }
                    }
                }
            }
        },true);
    }

    public void setStatusChangeListener(OnStatusChangeListener listener){
        mListeners.add(listener);
    }

    public void removeStatusChangeListener(OnStatusChangeListener listener){
        mListeners.remove(listener);
    }

    public interface OnStatusChangeListener {
        void requestReLogin(String message);
        void networkBroken();
    }

}
