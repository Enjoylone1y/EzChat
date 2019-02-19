package com.ezreal.ezchat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;
import android.widget.Toast;

import com.ezreal.ezchat.R;
import com.ezreal.ezchat.http.NimClientHandle;
import com.ezreal.ezchat.http.OnRegisterListener;
import com.ezreal.ezchat.utils.Constant;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.AuthService;
import com.suntek.commonlibrary.utils.SharedPreferencesUtil;
import com.suntek.commonlibrary.utils.TextUtils;
import com.suntek.commonlibrary.utils.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 更新密码
 * Created by wudeng on 2019/02/19.
 */

public class ChangePassActivity extends BaseActivity {

    @BindView(R.id.et_account)
    EditText mEtAccount;
    @BindView(R.id.et_pass_word)
    EditText mEtPass;
    @BindView(R.id.et_confirm_pass)
    EditText mEtConfirmPass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor(R.color.app_blue_color);
        setContentView(R.layout.activity_change_pass);
        setTitleBar("更新密码",true,false);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_update)
    public void register(){
        String account = mEtAccount.getText().toString().trim();
        final String pass = mEtPass.getText().toString().trim();
        String confirmPass = mEtConfirmPass.getText().toString().trim();
        if (TextUtils.isEmpty(confirmPass) || !confirmPass.equals(pass)){
            ToastUtils.showMessage(this,"确认密码为空或与密码不符");
            return;
        }

        NimClientHandle.getInstance().updateToken(account,pass, new OnRegisterListener() {
            @Override
            public void onSuccess() {
                ToastUtils.showMessage(ChangePassActivity.this,"更新成功");

                // 重新登录
                SharedPreferencesUtil.setStringSharedPreferences(ChangePassActivity.this,
                        Constant.LOCAL_LOGIN_TABLE,Constant.LOCAL_USER_TOKEN,pass);
                NIMClient.getService(AuthService.class).logout();
                Intent intent = new Intent(ChangePassActivity.this,LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            @Override
            public void onFailed(String message) {
                ToastUtils.showMessage(ChangePassActivity.this,"注册失败:" + message);
            }
        });
    }

    @OnClick(R.id.iv_back_btn)
    public void clickBack(){
        finish();
    }

}
