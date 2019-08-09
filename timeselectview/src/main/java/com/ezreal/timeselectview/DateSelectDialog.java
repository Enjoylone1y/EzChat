package com.ezreal.timeselectview;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;

/**
 * Created by wudeng on 2017/8/9.
 */

public class DateSelectDialog extends AlertDialog {

    private ImageView mIvCancel;
    private DateSelectView mDsvStartTime;
    private DateSelectView mDsvEndTime;
    private TextView mTvSelectAll;
    private TextView mTvConfirm;
    private OnDateSelectedListener mSelectedListener;

    public DateSelectDialog(@NonNull Context context) {
        this(context,0);
    }

    public DateSelectDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    public void setDateSelectedListener(OnDateSelectedListener listener){
        mSelectedListener = listener;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        View view =  LayoutInflater.from(getContext()).inflate(R.layout.dialog_date_select,null);
        addContentView(view,params);
        initView();
        initListener();
    }

    private void initView(){
        mIvCancel =  findViewById(R.id.iv_cancel);
        mDsvStartTime =  findViewById(R.id.dsv_start_time);
        mDsvEndTime =  findViewById(R.id.dsv_end_time);
        mTvSelectAll = findViewById(R.id.tv_select_all);
        mTvConfirm =  findViewById(R.id.tv_confirm);
    }

    private void initListener(){
        mIvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateSelectDialog.this.dismiss();
            }
        });

        mTvSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedListener != null){
                    mSelectedListener.onSelectedAll();
                    dismiss();
                }
            }
        });

        mTvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedListener != null){
                    mSelectedListener.onSelected(mDsvStartTime.getYear(),mDsvStartTime.getMonth(),
                            mDsvStartTime.getDay(),mDsvEndTime.getYear(),mDsvEndTime.getMonth(),
                            mDsvEndTime.getDay());
                    dismiss();
                }
            }
        });

    }

    public interface OnDateSelectedListener{
        void onSelected(int startYear,int startMonth,int startDay,
                         int endYear,int endMonth,int endDay);
        void onSelectedAll();
    }

}
