package com.ezreal.emojilibrary;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Emoji 表情包控件
 * Created by wudeng on 2017/11/3.
 */

public class EmojiLayout extends RelativeLayout {

    private ViewPager mViewPager;
    private IndicatorView mIndicatorView;
    private List<EmojiBean> mEmojiBeans;
    private List<View> mViewPageItems;
    private static final int COLUMNS = 7;
    private static final int ROWS = 3;
    private OnEmojiSelectListener mSelectListener;

    public EmojiLayout(Context context) {
        this(context,null);
    }

    public EmojiLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public EmojiLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mEmojiBeans = EmojiUtils.getEmojiBeans();
        View rootView = LayoutInflater.from(context).inflate(R.layout.layout_emoji, this,
                true);
        mViewPager = (ViewPager) rootView.findViewById(R.id.view_page);
        mIndicatorView = (IndicatorView) rootView.findViewById(R.id.indicator_view);
        initView();
    }


    private void initView(){
        // 根据表情数量计算表情页数
        int pagerCount = getPagerCount(mEmojiBeans.size());
        // 页面指示器初始化
        mIndicatorView.init(pagerCount);
        // 表情页初始化
        mViewPageItems = new ArrayList<>();
        for (int i = 0;i<pagerCount;i++){
            mViewPageItems.add(createViewPage(i * COLUMNS * ROWS));
        }
        ViewPageAdapter adapter = new ViewPageAdapter(mViewPageItems);
        mViewPager.setAdapter(adapter);

        // 根据页面切换，更新指示器
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int oldPosition = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mIndicatorView.playBy(oldPosition,position);
                oldPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private int getPagerCount(int emojiCount) {
        return emojiCount % (COLUMNS * ROWS) == 0 ? emojiCount / (COLUMNS * ROWS)
                : emojiCount / (COLUMNS * ROWS) + 1;
    }

    private View createViewPage(int offset){
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_grid_view, null, false);
        GridView gridView = (GridView) view.findViewById(R.id.grid_view);
        int toIndex = offset + ROWS * COLUMNS;
        if (toIndex > mEmojiBeans.size() - 1) {
            toIndex = mEmojiBeans.size() - 1;
        }

        final List<EmojiBean> currentList = mEmojiBeans.subList(offset, toIndex);

        gridView.setAdapter(new GridViewAdapter(getContext(),currentList));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mSelectListener != null){
                    if (position == parent.getAdapter().getCount() -1){
                        mSelectListener.emojiDelete();
                    }else {
                        mSelectListener.emojiSelect(currentList.get(position));
                    }
                }
            }
        });
        return gridView;
    }

    public void setSelectListener(OnEmojiSelectListener listener){
        this.mSelectListener = listener;
    }

    public interface OnEmojiSelectListener{
        void emojiSelect(EmojiBean emojiBean);
        void emojiDelete();
    }
}
