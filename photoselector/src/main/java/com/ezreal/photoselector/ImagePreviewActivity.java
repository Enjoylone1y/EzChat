package com.ezreal.photoselector;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * 照片预览界面
 * Created by wudeng on 2017/11/13.
 */

public class ImagePreviewActivity extends Activity {

    public static final int FLAG_SHOW_ITEM = 0x2000;
    public static final int FLAG_SHOW_LIST = 0x2001;

    private TextView mTvTitle;
    private ImageView mIvBack;
    private TextView mTvSend;
    private ViewPager mViewPager;
    private IndicatorView mIndicatorView;

    private List<ImageBean> mImageBeans;
    private List<View> mViews;
    private ViewPageAdapter mPageAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        findView();
        getImageFromIntent();
        showImage();
    }

    private void findView() {
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mTvTitle.setText("预览");

        mIvBack = (ImageView) findViewById(R.id.iv_back);
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePreviewActivity.this.setResult(RESULT_CANCELED);
                finish();
            }
        });

        mTvSend = (TextView) findViewById(R.id.tv_send);
        mTvSend.setText("发送");
        mTvSend.setTextColor(Color.WHITE);
        mTvSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePreviewActivity.this.setResult(RESULT_OK);
                finish();
            }
        });

        mIndicatorView = (IndicatorView) findViewById(R.id.indicator_view);
        mViewPager = (ViewPager) findViewById(R.id.view_page);
    }

    private void getImageFromIntent() {
        mImageBeans = new ArrayList<>();
        int flag = getIntent().getIntExtra("FLAG", FLAG_SHOW_ITEM);
        if (flag == FLAG_SHOW_ITEM) {
            ImageBean image = (ImageBean) getIntent().getSerializableExtra("IMAGE_ITEM");
            if (image != null) {
                mImageBeans.add(image);
            }
        } else if (flag == FLAG_SHOW_LIST) {
            List<ImageBean> list = (List<ImageBean>) getIntent().getSerializableExtra("IMAGE_LIST");
            if (list != null) {
                mImageBeans.addAll(list);
            }
        }
        createViews();
    }

    private void createViews() {
        mViews = new ArrayList<>();
        for (int i = 0; i < mImageBeans.size(); i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_image_pre,
                    null, false);
            PhotoView photoView = (PhotoView) view.findViewById(R.id.iv_img);
            photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            photoView.setMaxScale(4);
            photoView.enable();
            Glide.with(ImagePreviewActivity.this)
                    .load(mImageBeans.get(i).getPath()).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.bg_img_defalut)
                    .into(photoView);
            mViews.add(view);
        }
        mIndicatorView.init(mViews.size());
    }

    private void showImage() {
        mPageAdapter = new ViewPageAdapter(mViews);
        mViewPager.setAdapter(mPageAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            private int mOldPosition = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mIndicatorView.playBy(mOldPosition, position);
                mOldPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && keyCode == KeyEvent.KEYCODE_BACK){
            ImagePreviewActivity.this.setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class ViewPageAdapter extends PagerAdapter {

        private List<View> mViews;

        public ViewPageAdapter(List<View> views) {
            mViews = views;
        }

        @Override
        public int getCount() {
            return mViews != null ? mViews.size() : 0;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mViews.get(position));
            return mViews.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViews.get(position));
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
