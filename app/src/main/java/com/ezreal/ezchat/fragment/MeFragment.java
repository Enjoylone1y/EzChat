package com.ezreal.ezchat.fragment;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ezreal.ezchat.R;
import com.ezreal.ezchat.activity.AccountInfoActivity;
import com.ezreal.ezchat.bean.LocalAccountBean;
import com.ezreal.ezchat.handler.NimUserHandler;
import com.ezreal.ezchat.handler.NimUserHandler.OnInfoUpdateListener;
import com.joooonho.SelectableRoundedImageView;
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
    private LocalAccountBean mAccountBean;

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

    private void showOrRefreshView(){
        mAccountBean = NimUserHandler.getInstance().getLocalAccount();
        if (mAccountBean != null){
            ImageUtils.setImageByUrl(getContext(),mHeadView,
                    mAccountBean.getHeadImgUrl(),R.mipmap.app_logo_main);
            mTvName.setText(mAccountBean.getNick());
            mTvAccount.setText(mAccountBean.getAccount());
        }
    }
}
