package com.ezreal.ezchat.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.ezreal.ezchat.R;
import com.ezreal.ezchat.handler.NimUserHandler;
import com.ezreal.ezchat.utils.Constant;


public class SplashActivity extends BaseActivity {

    @SuppressLint("HandlerLeak")
    private  Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1){
                // 若已存在登陆信息则直接打开主页面，进入自动登陆流程，在主页面通过判断用户状态决定下一步操作
                if (NimUserHandler.getInstance().getMyAccount() != null){
                    startActivity(new Intent(SplashActivity.this,MainActivity.class));
                }else { // 若登陆信息为空，则启动登陆页面，进入手动登陆流程
                    startActivity(new Intent(SplashActivity.this,LoginActivity.class));
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mHandler.sendEmptyMessageDelayed(1,1000);
    }

}
