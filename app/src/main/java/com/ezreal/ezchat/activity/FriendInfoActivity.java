package com.ezreal.ezchat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ezreal.ezchat.R;
import com.joooonho.SelectableRoundedImageView;
import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.suntek.commonlibrary.utils.ImageUtils;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wudeng on 2017/8/28.
 */

public class FriendInfoActivity extends BaseActivity {

    public static final int FLAG_ADD_FRIEND = 10001;
    public static final int FLAG_SHOW_FRIEND = 10002;

    @BindView(R.id.iv_head_picture)
    SelectableRoundedImageView mHeadImg;
    @BindView(R.id.iv_person_sex)
    ImageView mIvPersonSex;
    @BindView(R.id.tv_remark)
    TextView mTvRemark;
    @BindView(R.id.tv_account)
    TextView mTvAccount;
    @BindView(R.id.tv_nike)
    TextView mTvNike;
    @BindView(R.id.tv_add_to_contract)
    TextView mTvAdd2Contract;
    @BindView(R.id.tv_start_chat)
    TextView mTvStartChat;
    @BindView(R.id.tv_video_chat)
    TextView mTvVideoChat;

    private NimUserInfo mNimUserInfo;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor(R.color.app_blue_color);
        setContentView(R.layout.activity_friend_info);
        setTitleBar("详细资料",true,true);
        ButterKnife.bind(this);
        bindViewByIntent();
    }


    private void bindViewByIntent(){
        Intent intent = getIntent();
        int flag = intent.getIntExtra("FLAG", FLAG_ADD_FRIEND);
        if ( flag == FLAG_ADD_FRIEND){
            mTvAdd2Contract.setVisibility(View.VISIBLE);
            mTvStartChat.setVisibility(View.GONE);
            mTvVideoChat.setVisibility(View.GONE);
        }else if (flag == FLAG_SHOW_FRIEND){
            mTvAdd2Contract.setVisibility(View.GONE);
            mTvStartChat.setVisibility(View.VISIBLE);
            mTvVideoChat.setVisibility(View.VISIBLE);
        }

        mNimUserInfo = (NimUserInfo) intent.getSerializableExtra("NimUserInfo");
        if (mNimUserInfo != null){
            ImageUtils.setImageByUrl(this,mHeadImg,mNimUserInfo.getAvatar(),R.mipmap.app_logo_main);
            if (mNimUserInfo.getGenderEnum() == GenderEnum.FEMALE){
                mIvPersonSex.setImageResource(R.mipmap.ic_woman);
            }else if (mNimUserInfo.getGenderEnum() == GenderEnum.MALE){
                mIvPersonSex.setImageResource(R.mipmap.ic_man);
            }
            mTvAccount.setText(mNimUserInfo.getAccount());
            mTvNike.setText(mNimUserInfo.getName());
            String remark = mNimUserInfo.getName();
            Map<String, Object> extensionMap = mNimUserInfo.getExtensionMap();
            if (extensionMap != null && extensionMap.containsKey("remark")){
                remark = extensionMap.get("remark").toString();
            }
            mTvRemark.setText(remark);
        }

    }

    /**
     * 设置备注信息
     */
    @OnClick(R.id.tv_set_remark)
    public void setRemark(){

    }

    /**
     * 添加好友
     */
    @OnClick(R.id.tv_add_to_contract)
    public void add2Contract(){
        Intent intent = new Intent(this,RequestFriendActivity.class);
        intent.putExtra("account",mNimUserInfo.getAccount());
        startActivity(intent);
    }

    /**
     * 跳转至聊天界面
     */
    @OnClick(R.id.tv_start_chat)
    public void startChat(){
        Intent intent = new Intent(this,P2PChatActivity.class);
        intent.putExtra("NimUserInfo",mNimUserInfo);
        startActivity(intent);
    }

    /**
     * 跳转到视频聊天界面
     */
    @OnClick(R.id.tv_video_chat)
    public void startVideoChat(){

    }

    @OnClick(R.id.iv_back_btn)
    public void backClick(){
        this.finish();
    }
}