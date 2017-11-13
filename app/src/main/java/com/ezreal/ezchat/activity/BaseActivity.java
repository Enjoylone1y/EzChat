package com.ezreal.ezchat.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.ezreal.ezchat.ChatApplication;
import com.ezreal.ezchat.R;
import com.suntek.commonlibrary.widget.SystemBarTintManager;


/**
 * Created by wudeng on 2017/8/22.
 */

public class BaseActivity extends AppCompatActivity {
    protected SystemBarTintManager mTintManager;
    protected ImageView mIvBack;
    protected ImageView mIvMenu;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            winParams.flags |= bits;
            win.setAttributes(winParams);
        }
        mTintManager = new SystemBarTintManager(this);
        mTintManager.setStatusBarTintEnabled(true);
        // 默认透明
        mTintManager.setStatusBarTintResource(android.R.color.transparent);
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
        if (mTintManager != null){
            mTintManager.setStatusBarTintResource(color);
        }
    }

    /**
     * 设置标题栏 需确定 该页面的layout布局文件 include title_layout
     * @param titleName 标题
     * @param showBackIcon 是否显示返回按钮
     * @param showMenuIcon 是否显示菜单按钮
     */
    protected void setTitleBar(String titleName, boolean showBackIcon,boolean showMenuIcon){
        try {
            TextView title = (TextView) findViewById(R.id.tv_title);
            title.setText(titleName);
            mIvBack = (ImageView) findViewById(R.id.iv_back_btn);
            mIvMenu = (ImageView) findViewById(R.id.iv_menu_btn);
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
