package com.ezreal.ezchat.fragment;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.ezreal.ezchat.R;
import com.ezreal.ezchat.activity.AccountInfoActivity;
import com.ezreal.ezchat.activity.ChangePassActivity;
import com.ezreal.ezchat.activity.LoginActivity;
import com.ezreal.ezchat.bean.LocalAccountBean;
import com.ezreal.ezchat.handler.NimUserHandler;
import com.ezreal.ezchat.handler.NimUserHandler.OnInfoUpdateListener;
import com.joooonho.SelectableRoundedImageView;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.AuthService;
import com.suntek.commonlibrary.utils.ImageUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wudeng on 2017/8/28.
 */

public class MeFragment extends BaseFragment {

    @BindView(R.id.iv_head_picture)
    SelectableRoundedImageView mHeadView;
    @BindView(R.id.tv_user_name)
    TextView mTvName;
    @BindView(R.id.tv_account)
    TextView mTvAccount;

    @Override
    public int setLayoutID() {
        return R.layout.fragment_me;
    }

    @Override
    public void initView(View rootView) {
        ButterKnife.bind(this,rootView);
        NimUserHandler.getInstance().setUpdateListeners(new OnInfoUpdateListener() {
            @Override
            public void myInfoUpdate() {
                showOrRefreshView();
            }
        });
        showOrRefreshView();
    }


    @OnClick(R.id.layout_account)
    public void openAccountInfo(){
        Intent intent = new Intent(getContext(), AccountInfoActivity.class);
        startActivity(intent);
    }


    @OnClick(R.id.tv_logout)
    public void logout(){
        NIMClient.getService(AuthService.class).logout();
        Intent intent = new Intent(getContext(),LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @OnClick(R.id.layout_change_pass)
    public void updatePass(){
        Intent intent = new Intent(getContext(),ChangePassActivity.class);
        startActivity(intent);
    }

    private void showOrRefreshView(){
        LocalAccountBean accountBean = NimUserHandler.getInstance().getLocalAccount();
        if (accountBean != null){
            ImageUtils.setImageByUrl(getContext(),mHeadView,
                    accountBean.getHeadImgUrl(),R.mipmap.app_logo_main);
            mTvName.setText(accountBean.getNick());
            mTvAccount.setText(accountBean.getAccount());
        }
    }
}
