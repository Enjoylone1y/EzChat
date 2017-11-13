package com.ezreal.photoselector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片选择器
 * Created by wudeng on 2017/11/7.
 */

public class PhotoSelector extends LinearLayout implements View.OnClickListener {

    private TextView mTvFolderName;
    private TextView mTvPreview;
    private ImageView mIvBack;
    private TextView mTvSend;
    private RadioButton mBtnSource;

    private RelativeLayout mLayoutBottom;
    private PopupWindow mFolderWindow;

    private List<FolderBean> mFolderBeans;
    private RecycleViewAdapter<FolderBean> mFolderAdapter;
    private int mSelFolderIndex = 0;
    private FolderBean mSelectedFolder;

    private RecyclerView mImageListView;
    private List<ImageBean> mImageBeans;
    private RecycleViewAdapter<ImageBean> mImageAdapter;

    private OnImageSelectorListener mSelectorListener;

    private boolean isSourceImage = false;
    private List<ImageBean> mSelectedImages = new ArrayList<>();

    private MyHandler mHandler = new MyHandler();

    public PhotoSelector(Context context) {
        this(context, null);
    }

    public PhotoSelector(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoSelector(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        initImageList();
        initPopupWindow();
        loadImageData();
    }

    private void initView() {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.layout_img_selector,
                this, true);
        mImageListView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mBtnSource = (RadioButton) rootView.findViewById(R.id.btn_source_img);
        mTvFolderName = (TextView) rootView.findViewById(R.id.tv_folder_name);
        mTvPreview = (TextView) rootView.findViewById(R.id.tv_preview);
        mIvBack = (ImageView) rootView.findViewById(R.id.iv_back);
        mTvSend = (TextView) rootView.findViewById(R.id.tv_send);

        mLayoutBottom = (RelativeLayout) rootView.findViewById(R.id.layout_bottom);

        mIvBack.setOnClickListener(this);
        mTvSend.setOnClickListener(this);
        mTvFolderName.setOnClickListener(this);
        mTvPreview.setOnClickListener(this);
        mBtnSource.setOnClickListener(this);

        // 文件选择按钮，只有在加载数据完成并且数据不为空的时候才可以点击
        mTvFolderName.setClickable(false);
        // 发送和预览按钮只有在有照片选择的情况下才可以点击
        mTvPreview.setClickable(false);
        mTvSend.setClickable(false);
    }

    private void initImageList() {
        mImageBeans = new ArrayList<>();
        mImageListView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mImageListView.addItemDecoration(new GridItemDecoration(getContext()));
        mImageAdapter = new RecycleViewAdapter<ImageBean>(getContext(), mImageBeans) {
            @Override
            public int setItemLayoutId(int position) {
                return R.layout.item_image;
            }

            @Override
            public void bindView(RViewHolder holder, int position) {
                ImageBean image = mImageBeans.get(position);
                ImageView imageView = holder.getImageView(R.id.iv_img);
                Glide.with(getContext())
                        .load(image.getPath()).diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.bg_img_defalut)
                        .into(imageView);
                RadioButton status = (RadioButton) holder.getConvertView()
                        .findViewById(R.id.img_sel_status);
                status.setChecked(image.isSelected());
            }
        };

        mImageAdapter.setItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(RViewHolder holder, int position) {
                ImageBean imageBean = mImageBeans.get(position);
                if (imageBean.isSelected()) {
                    imageBean.setSelected(false);
                    mSelectedImages.remove(imageBean);
                    mImageAdapter.notifyItemChanged(position);
                    updateBtnState();
                } else if (mSelectedImages.size() < 9) {
                    imageBean.setSelected(true);
                    mSelectedImages.add(imageBean);
                    mImageAdapter.notifyItemChanged(position);
                    updateBtnState();
                } else {
                    if (mSelectorListener != null) {
                        mSelectorListener.onOverSelect();
                    }
                }
            }
        });
        mImageListView.setAdapter(mImageAdapter);
    }


    private void initPopupWindow() {
        // 弹窗初始化,高度设置为屏幕的 3/4
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.popup_window,
                mLayoutBottom, false);
        int popupHeight = (int) (getContext().getResources().getDisplayMetrics().heightPixels * 0.75f);
        mFolderWindow = new PopupWindow(rootView, LayoutParams.MATCH_PARENT, popupHeight);
        mFolderWindow.setOutsideTouchable(true);
        mFolderWindow.setFocusable(true);

        // 文件夹列表初始化
        mFolderBeans = new ArrayList<>();
        RecyclerView folderListView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        folderListView.setLayoutManager(new LinearLayoutManager(getContext()));
        mFolderAdapter = new RecycleViewAdapter<FolderBean>(getContext(), mFolderBeans) {
            @Override
            public int setItemLayoutId(int position) {
                return R.layout.item_folder;
            }

            @Override
            public void bindView(RViewHolder holder, int position) {
                String name = mFolderBeans.get(position).getName();
                int size = mFolderBeans.get(position).getImageList().size();
                boolean selected = mFolderBeans.get(position).isSelected();
                ImageBean image = mFolderBeans.get(position).getImageList().get(0);

                holder.setText(R.id.tv_folder_name, name);
                holder.setText(R.id.tv_image_count, String.valueOf(size) + " 张");
                holder.setVisible(R.id.iv_sel_status, selected);
                ImageView imageView = holder.getImageView(R.id.iv_cover);
                Glide.with(getContext())
                        .load(image.getPath()).diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.bg_img_defalut)
                        .into(imageView);
            }
        };

        mFolderAdapter.setItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(RViewHolder holder, int position) {
                // 如果选择了新的文件夹
                if (position != mSelFolderIndex) {
                    // 将上个文件夹的选择状态去除
                    mFolderBeans.get(mSelFolderIndex).setSelected(false);
                    mFolderAdapter.notifyItemChanged(mSelFolderIndex);
                    // 将选择的文件夹做更新
                    mFolderBeans.get(position).setSelected(true);
                    mFolderAdapter.notifyItemChanged(position);

                    // 保存当前选择的文件夹
                    mSelFolderIndex = position;
                    mSelectedFolder = mFolderBeans.get(position);
                    mTvFolderName.setText(mSelectedFolder.getName());

                    // 关闭弹窗并更新图片列表
                    updateImageList();
                }

                hidePopup();
            }
        });
        folderListView.setAdapter(mFolderAdapter);
    }

    /**
     * 加载手机照片数据
     */
    private void loadImageData() {
        ImageUtils.loadImageList(getContext(), new ImageUtils.OnLoadImageCallBack() {
            @Override
            public void callBack(List<FolderBean> folderList) {
                if (folderList.isEmpty()) {
                    if (mSelectorListener != null) {
                        mSelectorListener.onImageEmpty();
                    }
                } else {
                    mSelectedFolder = folderList.get(0);
                    mSelFolderIndex = 0;
                    folderList.get(0).setSelected(true);
                    mFolderBeans.clear();
                    mFolderBeans.addAll(folderList);

                    // 加载数据在子线程完成的，需要使用mHandler去通知主线程跟新 UI
                    mHandler.sendEmptyMessage(0x100);
                }
            }
        });
    }


    @SuppressLint("HandlerLeak")
    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x100) {
                mFolderAdapter.notifyDataSetChanged();
                updateImageList();
                // 有照片，可以点击
                mTvFolderName.setClickable(true);
            }
        }
    }

    private void updateImageList() {
        mImageBeans.clear();
        mImageBeans.addAll(mSelectedFolder.getImageList());
        mImageAdapter.notifyDataSetChanged();
    }

    private void updateBtnState() {
        String size = String.valueOf(mSelectedImages.size());
        String tvSend = "发送(" + size + "/9)";
        String tvPre = "预览(" + size + "/9)";
        mTvSend.setText(tvSend);
        mTvPreview.setText(tvPre);

        if (mSelectedImages.isEmpty()) {
            mTvSend.setClickable(false);
            mTvSend.setTextColor(getResources().getColor(R.color.blue_gray));

            mTvPreview.setClickable(false);
            mTvPreview.setTextColor(getResources().getColor(R.color.blue_gray));
        } else {
            mTvSend.setClickable(true);
            mTvSend.setTextColor(getResources().getColor(R.color.colorAccent));

            mTvPreview.setClickable(true);
            mTvPreview.setTextColor(getResources().getColor(R.color.colorAccent));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_folder_name) {
            if (mFolderWindow.isShowing()) {
                hidePopup();
            } else {
                showPopup();
            }
        } else if (v.getId() == R.id.iv_back) {
            if (mSelectorListener != null) {
                mSelectorListener.onBackClick();
            }
        } else if (v.getId() == R.id.tv_send) {
            if (mSelectorListener != null) {
                mSelectorListener.onSend(mSelectedImages, isSourceImage);
            }
        } else if (v.getId() == R.id.tv_preview) {
            if (mSelectorListener != null) {
                mSelectorListener.onPreview(mSelectedImages, isSourceImage);
            }
        } else if (v.getId() == R.id.btn_source_img) {
            if (mBtnSource.isChecked()) {
                mBtnSource.setChecked(false);
                isSourceImage = false;
            } else {
                mBtnSource.setChecked(true);
                isSourceImage = false;
            }
        }
    }

    private void showPopup() {
        int[] location = new int[2];
        mLayoutBottom.getLocationOnScreen(location);
        mFolderWindow.showAtLocation(mLayoutBottom, Gravity.NO_GRAVITY, location[0],
                location[1] - mFolderWindow.getHeight());
    }

    private void hidePopup() {
        mFolderWindow.dismiss();
    }

    public void setSelectorListener(OnImageSelectorListener listener) {
        mSelectorListener = listener;

    }

    public interface OnImageSelectorListener {
        void onBackClick();

        void onOverSelect();

        void onImageEmpty();

        void onSend(List<ImageBean> selectedImages, boolean sourceSelected);

        void onPreview(List<ImageBean> selectedImages, boolean sourceSelected);
    }
}
