package com.ezreal.ezchat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.ezreal.ezchat.R;
import com.ezreal.photoselector.ImageBean;
import com.ezreal.photoselector.PhotoSelector;
import com.suntek.commonlibrary.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wudeng on 2017/11/7.
 */

public class SelectPhotoActivity extends BaseActivity {

    private PhotoSelector mPhotoSelector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor(R.color.app_blue_color);
        setContentView(R.layout.activity_select_photo);
        mPhotoSelector = (PhotoSelector) findViewById(R.id.photo_selector);
        mPhotoSelector.setSelectorListener(new PhotoSelector.OnImageSelectorListener() {
            @Override
            public void onBackClick() {
                setResult(RESULT_CANCELED);
                finish();
            }

            @Override
            public void onOverSelect() {
                ToastUtils.showMessage(SelectPhotoActivity.this,"最多只能选择9张喔~");
            }

            @Override
            public void onImageEmpty() {
                ToastUtils.showMessage(SelectPhotoActivity.this,"手机无照片~");
            }

            @Override
            public void onSend(List<ImageBean> selectedImages, boolean sourceSelected) {
                Intent intent = new Intent();
                String[] path = new String[selectedImages.size()];
                for (int i =0;i<selectedImages.size();i++){
                    path[i] = selectedImages.get(i).getPath();
                }
                intent.putExtra("images",path);
                setResult(RESULT_OK,intent);
                finish();
            }

            @Override
            public void onPreview(List<ImageBean> selectedImages, boolean sourceSelected) {
                ToastUtils.showMessage(SelectPhotoActivity.this,"正在开发中，敬请期待~");
            }
        });
    }
}
