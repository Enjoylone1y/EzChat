package com.ezreal.ezchat.activity;

import android.os.Bundle;

import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.ezreal.ezchat.R;
import com.ezreal.ezchat.http.NimClientHandle;
import com.ezreal.ezchat.http.OnRegisterListener;
import com.suntek.commonlibrary.utils.TextUtils;
import com.suntek.commonlibrary.utils.ToastUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 注册账户
 * Created by wudeng on 2017/8/24.
 */

public class RegisterActivity extends BaseActivity {

    @BindView(R.id.et_account)
    EditText mEtAccount;
    @BindView(R.id.et_user_name)
    EditText mEtName;
    @BindView(R.id.et_pass_word)
    EditText mEtPass;
    @BindView(R.id.et_confirm_pass)
    EditText mEtConfirmPass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor(R.color.app_blue_color);
        setContentView(R.layout.activity_register);
        setTitleBar("注册",true,false);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_register)
    public void register(){
        String account = mEtAccount.getText().toString().trim();
        String name = mEtName.getText().toString().trim();
        String pass = mEtPass.getText().toString().trim();
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
                ToastUtils.showMessage(RegisterActivity.this,"注册成功");
                finish();
            }

            @Override
            public void onFailed(String message) {
                ToastUtils.showMessage(RegisterActivity.this,"注册失败:" + message);
            }
        });
    }

    @OnClick(R.id.iv_back_btn)
    public void clickBack(){
        finish();
    }

}
