package com.ezreal.ezchat.bean;

import com.amap.api.services.core.LatLonPoint;

/**
 * 分享位置，单个位置点信息
 * Created by wudeng on 2017/9/19.
 */

public class LocationPoint {

    private String mId;
    private String mName;
    private String mAddress;
    private boolean mSelected;
    private LatLonPoint mPoint;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
    }

    public LatLonPoint getPoint() {
        return mPoint;
    }

    public void setPoint(LatLonPoint point) {
        mPoint = point;
    }
}
