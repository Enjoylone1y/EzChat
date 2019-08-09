package com.ezreal.ezchat.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.ezreal.ezchat.R;

import butterknife.ButterKnife;

/**
 * 群组列表页
 * created by wudeng on 2019/01/19.
 */
public class GroupListActivity extends BaseActivity {



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor(R.color.app_blue_color);
        setContentView(R.layout.activity_group_list);
        ButterKnife.bind(this);


    }
}
