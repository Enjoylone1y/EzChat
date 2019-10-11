package com.ezreal.ezchat.activity;

import android.os.Bundle;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ezreal.ezchat.ChatApplication;
import com.ezreal.ezchat.R;

/**
 * Created by wudeng on 2017/8/22.
 */

public class BaseActivity extends AppCompatActivity {

    protected ImageView mIvBack;
    protected ImageView mIvMenu;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChatApplication.getInstance().addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ChatApplication.getInstance().finishActivity(this);
    }

    /**
     * 设置当前页面状态栏颜色
     * @param color 颜色值 R.color.app_blue_color
     */
    protected void setStatusBarColor(int color){

    }

    /**
     * 设置标题栏 需确定 该页面的layout布局文件 include title_layout
     * @param titleName 标题
     * @param showBackIcon 是否显示返回按钮
     * @param showMenuIcon 是否显示菜单按钮
     */
    protected void setTitleBar(String titleName, boolean showBackIcon,boolean showMenuIcon){
        try {
            TextView title =  findViewById(R.id.tv_title);
            title.setText(titleName);
            mIvBack =  findViewById(R.id.iv_back_btn);
            mIvMenu =  findViewById(R.id.iv_menu_btn);
            if (showBackIcon){
                mIvBack.setVisibility(View.VISIBLE);
            }
            if (showMenuIcon){
                mIvMenu.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
