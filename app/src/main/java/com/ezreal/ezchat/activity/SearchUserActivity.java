package com.ezreal.ezchat.activity;

import android.content.Intent;
import android.os.Bundle;

import android.text.TextUtils;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.ezreal.ezchat.R;
import com.ezreal.ezchat.handler.NimUserHandler;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.suntek.commonlibrary.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 根据账户搜索用户
 * Created by wudeng on 2017/8/28.
 */

public class SearchUserActivity extends BaseActivity {

    @BindView(R.id.et_user_account)
    EditText mEtUserAccount;

    private static final String TAG = SearchUserActivity.class.getSimpleName();

    private RequestCallback<List<NimUserInfo>> mRequestCallback;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor(R.color.app_blue_color);
        setContentView(R.layout.activity_search_user);
        setTitleBar("添加朋友",true,false);
        ButterKnife.bind(this);
        initCallBack();
    }

    private void initCallBack(){
        mRequestCallback = new RequestCallback<List<NimUserInfo>>() {

            @Override
            public void onSuccess(List<NimUserInfo> param) {
                if (param.isEmpty()){
                    ToastUtils.showMessage(SearchUserActivity.this,"查无此人喔，请检查后再试~~");
                    return;
                }
                Intent intent = new Intent(SearchUserActivity.this,FriendInfoActivity.class);
                intent.putExtra("FLAG",FriendInfoActivity.FLAG_ADD_FRIEND);
                intent.putExtra("NimUserInfo",param.get(0));
                startActivity(intent);
            }

            @Override
            public void onFailed(int code) {
                ToastUtils.showMessage(SearchUserActivity.this,"搜索失败，返回码：" + code);
            }

            @Override
            public void onException(Throwable exception) {
                ToastUtils.showMessage(SearchUserActivity.this,"搜索出错：" + exception.getMessage());
            }
        };
    }

    @OnClick(R.id.iv_search)
    public void searchFriend(){
        String account = mEtUserAccount.getText().toString().trim();
        if (TextUtils.isEmpty(account)){
            ToastUtils.showMessage(SearchUserActivity.this,"账号不能为空");
            return;
        }
        if (account.equals(NimUserHandler.getInstance().getMyAccount())){
            ToastUtils.showMessage(SearchUserActivity.this,"不能添加自己为好友");
            return;
        }
        List<String> accounts = new ArrayList<>(1);
        accounts.add(account);
        NIMClient.getService(UserService.class).fetchUserInfo(accounts).setCallback(mRequestCallback);
    }

    @OnClick(R.id.iv_back_btn)
    public void backOnClick(){
        finish();
    }

}
