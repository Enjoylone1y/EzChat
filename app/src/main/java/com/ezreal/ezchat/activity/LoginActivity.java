package com.ezreal.ezchat.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ezreal.ezchat.ChatApplication;
import com.ezreal.ezchat.R;
import com.ezreal.ezchat.handler.NimUserHandler;
import com.ezreal.ezchat.http.NimClientHandle;
import com.ezreal.ezchat.http.OnRegisterListener;
import com.ezreal.ezchat.utils.Constant;
import com.ezreal.ezchat.utils.ConvertUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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


    @BindView(R.id.layout_login)
    RelativeLayout mLayoutLogin;
    @BindView(R.id.layout_register)
    RelativeLayout mLayoutRegister;
    @BindView(R.id.et_user_account)
    EditText mEtLoginAccount;
    @BindView(R.id.et_pass_word)
    EditText mEtLoginPassword;
    @BindView(R.id.fab_register)
    FloatingActionButton mBtnRegister;

    @BindView(R.id.et_register_account)
    EditText mEtRegisterAccount;
    @BindView(R.id.et_register_name)
    EditText mEtRegisterName;
    @BindView(R.id.et_register_pass)
    EditText mEtRegisterPass;
    @BindView(R.id.et_register_confirm_pass)
    EditText mEtConfirmPass;


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
        mEtLoginPassword.setOnTouchListener(new MyTouchListener());
        initPermission();

        String id = SharedPreferencesUtil.getStringSharedPreferences(this, Constant.LOCAL_LOGIN_TABLE,
                Constant.LOCAL_USER_ACCOUNT);
        String token = SharedPreferencesUtil.getStringSharedPreferences(this, Constant.LOCAL_LOGIN_TABLE,
                Constant.LOCAL_USER_TOKEN);
        if (!TextUtils.isEmpty(id)) {
            mEtLoginAccount.setText(id);
            if (!TextUtils.isEmpty(token)) {
                mEtLoginPassword.setText(token);
            }
        }
    }


    /**
     * 检查，申请权限
     */
    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean has = PermissionUtils.checkPermissions(this, BASIC_PERMISSIONS);
            if (!has) {
                PermissionUtils.requestPermissions(this, PERMISSION_REQUEST_CODE,
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
        if (requestCode == PERMISSION_REQUEST_CODE) {
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
    public void login() {
        String account = mEtLoginAccount.getText().toString().trim();
        String pass = mEtLoginPassword.getText().toString().trim();
        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(pass)) {
            ToastUtils.showMessage(this, "账号或密码为空~");
            return;
        }
        RequestCallback<LoginInfo> callBack = new RequestCallback<LoginInfo>() {
            @Override
            public void onSuccess(LoginInfo loginInfo) {
                isLogin = false;
                // 保存登录信息
                saveLoginInfo(loginInfo);
                // 转入主页面
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }

            @Override
            public void onFailed(int code) {
                isLogin = false;
                ToastUtils.showMessage(LoginActivity.this,
                        "登录失败：" + ConvertUtils.code2String(code));
            }

            @Override
            public void onException(Throwable exception) {
                isLogin = false;
                ToastUtils.showMessage(LoginActivity.this,
                        "登录出错：" + exception.getMessage());
            }
        };
        LoginInfo loginInfo = new LoginInfo(account, pass);
        mLoginFuture = NIMClient.getService(AuthService.class).login(loginInfo);
        isLogin = true;
        mLoginFuture.setCallback(callBack);
    }


    @OnClick(R.id.btn_register)
    public void register(){
        String account = mEtRegisterAccount.getText().toString().trim();
        String name = mEtRegisterName.getText().toString().trim();
        String pass = mEtRegisterPass.getText().toString().trim();
        String confirmPass = mEtConfirmPass.getText().toString().trim();
        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(name) || TextUtils.isEmpty(pass)){
            Toast.makeText(this,"请将信息填写完整",Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(confirmPass) || !confirmPass.equals(pass)){
            ToastUtils.showMessage(this,"确认密码为空或与密码不符");
            return;
        }

        NimClientHandle.getInstance().register(account,pass, name, new OnRegisterListener() {
            @Override
            public void onSuccess() {
                ToastUtils.showMessage(LoginActivity.this,"注册成功");
                mEtLoginAccount.setText(account);
                mEtLoginPassword.setText(pass);
                hindRegisterLayout();
            }

            @Override
            public void onFailed(String message) {
                ToastUtils.showMessage(LoginActivity.this,"注册失败:" + message);
            }
        });
    }


    @OnClick(R.id.fab_register)
    public void openRegisterLayout() {

        // 按钮移动到屏幕中央
        int[] origin = getCenterCoord(mBtnRegister);
        int[] distance = {getResources().getDisplayMetrics().widthPixels / 2,
                getResources().getDisplayMetrics().heightPixels / 2};
        int changeX = distance[0] - origin[0];
        int changeY = distance[1] - origin[1];
        ObjectAnimator translationX = ObjectAnimator.ofFloat(mBtnRegister, "translationX",
                0, changeX);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(mBtnRegister, "translationY",
                0, changeY);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mBtnRegister, "alpha", 1, 0.5f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.play(translationX).with(translationY).with(alpha);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                showRegisterLayout();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();

    }

    // 显示注册布局
    private void showRegisterLayout() {

        double hypot = Math.hypot(mLayoutRegister.getWidth(), mLayoutRegister.getHeight());
        int[] center = {getResources().getDisplayMetrics().widthPixels / 2,
                getResources().getDisplayMetrics().heightPixels / 2};
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(mLayoutRegister,
                center[0], center[1], 0, (int) hypot);
        circularReveal.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mLayoutRegister.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        circularReveal.setDuration(300);
        circularReveal.setInterpolator(new AccelerateDecelerateInterpolator());
        circularReveal.start();
    }


    @OnClick(R.id.fab_close)
    public void cancelRegister() {
        hindRegisterLayout();
    }

    private void hindRegisterLayout(){

        // 清空数据
        mEtRegisterAccount.getText().clear();
        mEtRegisterName.getText().clear();
        mEtRegisterPass.getText().clear();
        mEtConfirmPass.getText().clear();

        double hypot = Math.hypot(mLayoutRegister.getWidth(), mLayoutRegister.getHeight());
        int[] center = {getResources().getDisplayMetrics().widthPixels / 2,
                getResources().getDisplayMetrics().heightPixels / 2};
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(mLayoutRegister,
                center[0], center[1], (int) hypot, 0);
        circularReveal.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mLayoutRegister.setVisibility(View.INVISIBLE);
                resetRegisterButton();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        circularReveal.setDuration(300);
        circularReveal.setInterpolator(new LinearInterpolator());
        circularReveal.start();
    }

    private void resetRegisterButton() {
        ObjectAnimator translationX = ObjectAnimator.ofFloat(mBtnRegister, "translationX",
                mBtnRegister.getTranslationX(),0 );
        ObjectAnimator translationY = ObjectAnimator.ofFloat(mBtnRegister, "translationY",
                mBtnRegister.getTranslationY(),0 );
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mBtnRegister, "alpha", 0.5f, 1);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.play(translationX).with(translationY).with(alpha);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
    }

    private int[] getCenterCoord(View view) {
        int[] leftTop = {-1, -1};
        view.getLocationInWindow(leftTop);
        int centerX = view.getWidth() / 2 + leftTop[0];
        int centerY = view.getHeight() / 2 + leftTop[1];
        return new int[]{centerX, centerY};
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isLogin) {
                mLoginFuture.abort();
            } else {
                backDoubleExit();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void backDoubleExit() {
        mKeyBackCount++;
        if (mKeyBackCount == 1) {
            ToastUtils.showMessage(LoginActivity.this, "再点一次退出程序~~");
        } else if (mKeyBackCount == 2) {
            ChatApplication.getInstance().AppExit();
        }
    }


    private void saveLoginInfo(LoginInfo info) {
        SharedPreferencesUtil.setStringSharedPreferences(this, Constant.LOCAL_LOGIN_TABLE,
                Constant.LOCAL_USER_ACCOUNT, info.getAccount());
        SharedPreferencesUtil.setStringSharedPreferences(this, Constant.LOCAL_LOGIN_TABLE,
                Constant.LOCAL_USER_TOKEN, info.getToken());
        NimUserHandler.getInstance().setMyAccount(info.getAccount());
    }

    private class MyTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.post(() -> {
                    int keyBoardHeight = SystemUtils.getKeyBoardHeight(LoginActivity.this);
                    if (keyBoardHeight != 0) {
                        SharedPreferencesUtil.setIntSharedPreferences(LoginActivity.this,
                                Constant.OPTION_TABLE, Constant.OPTION_KEYBOARD_HEIGHT, keyBoardHeight);
                    }
                });
            }
            v.performClick();
            return false;
        }
    }
}
