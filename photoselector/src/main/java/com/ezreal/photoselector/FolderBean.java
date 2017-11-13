package com.ezreal.photoselector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wudeng on 2017/11/7.
 */

public class FolderBean {

    private String name;
    private List<ImageBean> mImageList;
    private boolean isSelected = false;

    public FolderBean(String name) {
        this.name = name;
    }

    public FolderBean(String name, List<ImageBean> imageList) {
        this.name = name;
        mImageList = imageList;
    }

    public void addImage(ImageBean imageBean){
        if (mImageList == null){
            mImageList = new ArrayList<>();
        }
        mImageList.add(imageBean);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ImageBean> getImageList() {
        return mImageList;
    }

    public void setImageList(List<ImageBean> imageList) {
        mImageList = imageList;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
