package com.ezreal.ezchat.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ezreal.ezchat.ChatApplication;
import com.ezreal.ezchat.R;
import com.ezreal.ezchat.handler.NimUserHandler;
import com.ezreal.ezchat.utils.Constant;
import com.ezreal.ezchat.utils.ConvertUtils;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.suntek.commonlibrary.utils.PermissionUtils;
import com.suntek.commonlibrary.utils.SharedPreferencesUtil;
import com.suntek.commonlibrary.utils.SystemUtils;
import com.suntek.commonlibrary.utils.TextUtils;
import com.suntek.commonlibrary.utils.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wudeng on 2017/8/24.
 */

public class LoginActivity extends BaseActivity {

    @BindView(R.id.et_user_account)
    EditText mEtUserAccount;
    @BindView(R.id.et_pass_word)
    EditText mEtPassWord;

    private final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int PERMISSION_REQUEST_CODE = 100001;

    private AbortableFuture<LoginInfo> mLoginFuture;
    private static boolean isLogin = false;
    private static int mKeyBackCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mEtPassWord.setOnTouchListener(new MyTouchListener());
        initPermission();

        String id = SharedPreferencesUtil.getStringSharedPreferences(this, Constant.LOCAL_LOGIN_TABLE,
                Constant.LOCAL_USER_ACCOUNT);
        String token =  SharedPreferencesUtil.getStringSharedPreferences(this, Constant.LOCAL_LOGIN_TABLE,
                Constant.LOCAL_USER_TOKEN);
        if (!TextUtils.isEmpty(id)){
            mEtUserAccount.setText(id);
            if (!TextUtils.isEmpty(token)){
                mEtPassWord.setText(token);
            }
        }
    }

    /**
     * 检查，申请权限
     */
    private void initPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            boolean has = PermissionUtils.checkPermissions(this, BASIC_PERMISSIONS);
            if (!has){
                PermissionUtils.requestPermissions(this,PERMISSION_REQUEST_CODE,
                        BASIC_PERMISSIONS);
            }
        }

    }

    /**
     * 跳转到权限设置页面返回后再次检查
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        initPermission();
    }

    /**
     * 权限授予结果回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE){
            PermissionUtils.dealPermissionResult(LoginActivity.this, permissions, grantResults,
                    new PermissionUtils.RequestPermissionCallBack() {
                        @Override
                        public void onGrant(String... permissions) {

                        }

                        @Override
                        public void onDenied(String... permissions) {

                        }

                        @Override
                        public void onDeniedAndNeverAsk(String... permissions) {

                        }
                    });
        }
    }


    @OnClick(R.id.tv_btn_login)
    public void login(){
        String account = mEtUserAccount.getText().toString().trim();
        String pass = mEtPassWord.getText().toString().trim();
        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(pass)){
            ToastUtils.showMessage(this,"账号或密码为空~");
            return;
        }
        RequestCallback<LoginInfo> callBack = new RequestCallback<LoginInfo>() {
            @Override
            public void onSuccess(LoginInfo loginInfo) {
                isLogin = false;
                // 保存登录信息
                saveLoginInfo(loginInfo);
                // 转入主页面
                startActivity(new Intent(LoginActivity.this,MainActivity.class));
            }

            @Override
            public void onFailed(int code) {
                isLogin = false;
                ToastUtils.showMessage(LoginActivity.this,
                        "登录失败："+ ConvertUtils.code2String(code));
            }

            @Override
            public void onException(Throwable exception) {
                isLogin = false;
                ToastUtils.showMessage(LoginActivity.this,
                        "登录出错："+exception.getMessage());
            }
        };
        LoginInfo loginInfo = new LoginInfo(account,pass);
        mLoginFuture = NIMClient.getService(AuthService.class).login(loginInfo);
        isLogin = true;
        mLoginFuture.setCallback(callBack);
    }

    @OnClick(R.id.tv_btn_register)
    public void startRegister(){
        startActivity(new Intent(this,
                RegisterActivity.class));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            if (isLogin){
                mLoginFuture.abort();
            }else {
                backDoubleExit();
            }
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    private void backDoubleExit(){
        mKeyBackCount++;
        if (mKeyBackCount == 1){
            ToastUtils.showMessage(LoginActivity.this,"再点一次退出程序~~");
        }else if (mKeyBackCount == 2){
            ChatApplication.getInstance().AppExit();
        }
    }


    private void saveLoginInfo(LoginInfo info){
        SharedPreferencesUtil.setStringSharedPreferences(this, Constant.LOCAL_LOGIN_TABLE,
                Constant.LOCAL_USER_ACCOUNT,info.getAccount());
        SharedPreferencesUtil.setStringSharedPreferences(this, Constant.LOCAL_LOGIN_TABLE,
                Constant.LOCAL_USER_TOKEN,info.getToken());
        NimUserHandler.getInstance().setMyAccount(info.getAccount());
    }

    private class MyTouchListener implements View.OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP){
                v.post(new Runnable() {
                    @Override
                    public void run() {
                        int keyBoardHeight = SystemUtils.getKeyBoardHeight(LoginActivity.this);
                        if (keyBoardHeight != 0){
                            SharedPreferencesUtil.setIntSharedPreferences(LoginActivity.this,
                                    Constant.OPTION_TABLE,Constant.OPTION_KEYBOARD_HEIGHT,keyBoardHeight);
                        }
                    }
                });
            }
            v.performClick();
            return false;
        }
    }
}
