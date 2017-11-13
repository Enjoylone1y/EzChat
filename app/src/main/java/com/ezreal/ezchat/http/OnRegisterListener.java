package com.ezreal.ezchat.http;

/**
 * Created by wudeng on 2017/8/25.
 */

public interface OnRegisterListener {

    void onSuccess();

    void onFailed(String message);
}
