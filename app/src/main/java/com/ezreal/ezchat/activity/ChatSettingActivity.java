package com.ezreal.ezchat.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.ezreal.ezchat.R;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.suntek.commonlibrary.utils.ImageUtils;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 聊天设置 页面
 * Created by wudeng on 2017/11/10.
 */

public class ChatSettingActivity extends BaseActivity {

    @BindView(R.id.user_list)
    TagFlowLayout mListView;

    private List<NimUserInfo> mUserList;
    private TagAdapter<NimUserInfo> mListAdapter;
    private LayoutInflater mInflater;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor(R.color.app_blue_color);
        setContentView(R.layout.activity_chat_setting);
        ButterKnife.bind(this);
        setTitleBar("聊天信息",true,false);
        initView();

    }

    private void initView(){
        mUserList = new ArrayList<>();
        mInflater = LayoutInflater.from(this);
        mListAdapter = new TagAdapter<NimUserInfo>(mUserList) {
            @Override
            public View getView(FlowLayout parent, int position, NimUserInfo nimUserInfo) {
                View view = mInflater.inflate(R.layout.item_user,null,false);
                TextView name = (TextView) view.findViewById(R.id.tv_user_name);
                ImageView headImage = (ImageView) view.findViewById(R.id.iv_head_picture);
                ImageUtils.setImageByUrl(ChatSettingActivity.this,headImage,
                        nimUserInfo.getAvatar(),R.mipmap.bg_img_defalut);
                name.setText(nimUserInfo.getName());
                return view;
            }
        };
        mListView.setAdapter(mListAdapter);
        mListView.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {
                return false;
            }
        });

    }

}
