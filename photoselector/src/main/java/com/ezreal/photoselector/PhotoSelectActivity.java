package com.ezreal.photoselector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片选择页面
 * Created by wudeng on 2017/11/13.
 */

public class PhotoSelectActivity extends Activity implements View.OnClickListener{

    private static final int REQUEST_SHOW_ITEM = 0x3000;
    private static final int REQUEST_SHOW_LIST = 0x3001;

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
    private boolean isSourceImage = false;
    private List<ImageBean> mSelectedImages = new ArrayList<>();
    private ImageBean mShowItem;
    private MyHandler mHandler = new MyHandler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_selector);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        initView();
        initImageList();
        initPopupWindow();
        loadImageData();
    }

    private void initView() {
        mImageListView = findViewById(R.id.recycler_view);
        mBtnSource =  findViewById(R.id.btn_source_img);
        mTvFolderName = findViewById(R.id.tv_folder_name);
        mTvPreview =  findViewById(R.id.tv_preview);
        mIvBack =  findViewById(R.id.iv_back);
        mTvSend =  findViewById(R.id.tv_send);

        mLayoutBottom =  findViewById(R.id.layout_bottom);

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

    /**
     * 初始化照片选择列表，使用 recycler view 显示
     */
    private void initImageList() {
        mImageBeans = new ArrayList<>();
        mImageListView.setLayoutManager(new GridLayoutManager(this, 3));
        mImageListView.addItemDecoration(new GridItemDecoration(this));
        mImageAdapter = new RecycleViewAdapter<ImageBean>(this, mImageBeans) {
            @Override
            public int setItemLayoutId(int position) {
                return R.layout.item_image_sel;
            }

            @Override
            public void bindView(RViewHolder holder, final int position) {
                // 使用Glide加载照片，它有缓存策略，避免 OOM
                final ImageBean item = mImageBeans.get(position);
                ImageView imageView = holder.getImageView(R.id.iv_img);
                Glide.with(PhotoSelectActivity.this)
                        .load(item.getPath()).diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.bg_img_defalut)
                        .into(imageView);

                // 单击照片本身，在预览窗口打开该照片
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mShowItem = item;
                        previewByItem();
                    }
                });

                // 照片右上角的选择器，根据照片被选择与否显示不同状态 icon
                final RadioButton radioButton = (RadioButton) holder.getConvertView()
                        .findViewById(R.id.img_sel_status);
                radioButton.setChecked(item.isSelected());

                // 选择框单击事件监听，用于更新照片选择状态
                radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImageBean imageBean = mImageBeans.get(position);
                        if (imageBean.isSelected()) {
                            radioButton.setChecked(false);
                            imageBean.setSelected(false);
                            mSelectedImages.remove(imageBean);
                            updateBtnState();
                        } else if (mSelectedImages.size() < 9) {
                            radioButton.setChecked(true);
                            imageBean.setSelected(true);
                            mSelectedImages.add(imageBean);
                            updateBtnState();
                        } else {
                            radioButton.setChecked(false);
                            Toast.makeText(PhotoSelectActivity.this,
                                    "一次只能选择9张喔~",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };
        mImageListView.setAdapter(mImageAdapter);
    }

    /**
     * 初始化 照片文件夹 选择器（popupWindow）
     */
    private void initPopupWindow() {
        // 弹窗初始化,高度设置为屏幕的 3/4
        View rootView = LayoutInflater.from(this).inflate(R.layout.popup_window,
                mLayoutBottom, false);
        int popupHeight = (int) (this.getResources().getDisplayMetrics().heightPixels * 0.75f);
        mFolderWindow = new PopupWindow(rootView, LinearLayout.LayoutParams.MATCH_PARENT, popupHeight);
        mFolderWindow.setOutsideTouchable(true);
        mFolderWindow.setFocusable(true);
        mFolderWindow.setAnimationStyle(R.style.popup_window_anim);
        // 在PopupWindow隐藏后，将背景恢复正常亮度
        mFolderWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                lightOn();
            }
        });

        // 文件夹列表初始化
        mFolderBeans = new ArrayList<>();
        RecyclerView folderListView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        folderListView.setLayoutManager(new LinearLayoutManager(this));
        mFolderAdapter = new RecycleViewAdapter<FolderBean>(this, mFolderBeans) {
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
                Glide.with(PhotoSelectActivity.this)
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
        ImageUtils.loadImageList(this, new ImageUtils.OnLoadImageCallBack() {
            @Override
            public void callBack(List<FolderBean> folderList) {
                if (folderList.isEmpty()) {
                    Toast.makeText(PhotoSelectActivity.this,
                            "无照片~",Toast.LENGTH_SHORT).show();
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

    /**
     * 根据选择的文件夹，更新图片列表
     */
    private void updateImageList() {
        mImageBeans.clear();
        mImageBeans.addAll(mSelectedFolder.getImageList());
        mImageAdapter.notifyDataSetChanged();
    }

    /**
     * 根据选择照片的数量，更新 “发送” 和 “预览” 按钮的显示文字和可点击状态
     */
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
           this.setResult(RESULT_CANCELED);
           this.finish();
        } else if (v.getId() == R.id.tv_send) {
            sendImage();
        } else if (v.getId() == R.id.tv_preview) {
            previewByBtn();
        } else if (v.getId() == R.id.btn_source_img) {
            if (mBtnSource.isChecked()) {
                mBtnSource.setChecked(false);
                isSourceImage = false;
            } else {
                mBtnSource.setChecked(true);
                isSourceImage = true;
            }
        }
    }

    /**
     * 显示和隐藏文件夹选择框 显示在底部布局之上
     */
    private void showPopup() {
        int[] location = new int[2];
        mLayoutBottom.getLocationOnScreen(location);
        mFolderWindow.showAtLocation(mLayoutBottom, Gravity.NO_GRAVITY, location[0],
                location[1] - mFolderWindow.getHeight());
        lightOff();
    }

    private void hidePopup() {
        mFolderWindow.dismiss();
    }

    /**
     *  popupWindow 显示的时候 将图片列表区域变暗
     *  popupWindow 隐藏的时候恢复
     */

    private void lightOn(){
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.alpha = 1.0f;
        getWindow().setAttributes(attributes);
    }

    private void lightOff(){
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.alpha = 0.3f;
        getWindow().setAttributes(attributes);
    }


    /**
     * 单击单张图片时，调整到预览页面显示该图
     */
    private void previewByItem(){
        Intent intent = new Intent(this,ImagePreviewActivity.class);
        intent.putExtra("FLAG",ImagePreviewActivity.FLAG_SHOW_ITEM);
        intent.putExtra("IMAGE_ITEM",mShowItem);
        startActivityForResult(intent,REQUEST_SHOW_ITEM);
    }

    /**
     * 单击“预览”按钮时，跳转到预览页面显示所有已选中的图片
     */
    private void previewByBtn(){
        Intent intent = new Intent(this,ImagePreviewActivity.class);
        intent.putExtra("FLAG",ImagePreviewActivity.FLAG_SHOW_LIST);
        intent.putExtra("IMAGE_LIST", (Serializable) mSelectedImages);
        startActivityForResult(intent,REQUEST_SHOW_LIST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 预览界面点击了“发送”按钮
        if (resultCode == RESULT_OK){
            if (requestCode == REQUEST_SHOW_ITEM){
                if (mShowItem != null){
                    mSelectedImages.clear();
                    mSelectedImages.add(mShowItem);
                    sendImage();
                }
            }else if (requestCode == REQUEST_SHOW_LIST){
                sendImage();
            }
        }
    }

    /**
     * 将已选文件的路径集返回给请求页面
     */
    private void sendImage(){
        Intent intent = new Intent();
        String[] path = new String[mSelectedImages.size()];
        for (int i =0;i<mSelectedImages.size();i++){
            path[i] = mSelectedImages.get(i).getPath();
        }
        intent.putExtra("images",path);
        setResult(RESULT_OK,intent);
        finish();
    }

}
