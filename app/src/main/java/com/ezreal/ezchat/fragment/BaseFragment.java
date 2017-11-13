package com.ezreal.ezchat.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by wudeng on 2017/8/28.
 */

public abstract class BaseFragment extends Fragment {

    protected View mRootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(setLayoutID(), container, false);
            initView(mRootView);
        }
        //缓存的rootView需要判断是否已经被加过parent
        //如果有parent需要从parent删除，要不然会发生这个rootView已经有parent的错误。
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }
        return mRootView;
    }

    public abstract int setLayoutID();
    public abstract void initView(View rootView);

}
