package com.ezreal.ezchat.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.ezreal.ezchat.R;


/**
 * Created by wudeng on 2017/9/4.
 */

public class MsgHeadView extends LinearLayout {

    private static final String TAG = MsgHeadView.class.getSimpleName();
    private LinearLayout mContainer;
    private int mHeadHeight;

    public MsgHeadView(Context context) {
        this(context,null);
    }

    public MsgHeadView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MsgHeadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mContainer = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.msg_head_view, null);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 0);
        this.setLayoutParams(lp);
        this.setPadding(0, 0, 0, 0);
        addView(mContainer, new LayoutParams(LayoutParams.MATCH_PARENT, 0));
        setGravity(Gravity.BOTTOM);
        int height = View.MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);
        int width = View.MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);
        mContainer.measure(width,height);
        mHeadHeight = mContainer.getMeasuredHeight();
    }

    public int getHeadHeight(){
        return mHeadHeight;
    }

    private void smoothScrollTo(int destHeight) {
        ValueAnimator animator = ValueAnimator.ofInt(getVisibleHeight(), destHeight);
        animator.setDuration(300).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                setVisibleHeight((int) animation.getAnimatedValue());
            }
        });
        animator.start();
    }

    public void setVisibleHeight(int height) {
        if (height < 0) height = 0;
        LayoutParams lp = (LayoutParams) mContainer .getLayoutParams();
        lp.height = height;
        mContainer.setLayoutParams(lp);
    }

    public int getVisibleHeight() {
        LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
        return lp.height;
    }

}
