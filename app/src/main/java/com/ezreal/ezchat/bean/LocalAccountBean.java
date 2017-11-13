package com.ezreal.ezchat.bean;

import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;

import java.io.Serializable;

/**
 * Created by wudeng on 2017/9/1.
 */

public class LocalAccountBean implements Serializable {

    private String mHeadImgUrl;
    private String mAccount;
    private String mNick;
    private GenderEnum mGenderEnum;
    private String mBirthDay;
    private String mLocation;
    private String mSignature;

    public String getHeadImgUrl() {
        return mHeadImgUrl;
    }

    public void setHeadImgUrl(String headImgUrl) {
        mHeadImgUrl = headImgUrl;
    }

    public String getAccount() {
        return mAccount;
    }

    public void setAccount(String account) {
        mAccount = account;
    }

    public String getNick() {
        return mNick;
    }

    public void setNick(String nick) {
        mNick = nick;
    }

    public GenderEnum getGenderEnum() {
        return mGenderEnum;
    }

    public void setGenderEnum(GenderEnum genderEnum) {
        mGenderEnum = genderEnum;
    }

    public String getBirthDay() {
        return mBirthDay;
    }

    public void setBirthDay(String birthDay) {
        mBirthDay = birthDay;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public String getSignature() {
        return mSignature;
    }

    public void setSignature(String signature) {
        mSignature = signature;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return builder.append("account = ")
                .append(mAccount)
                .append(",url = ")
                .append(mHeadImgUrl)
                .append(",location = ")
                .append(mLocation)
                .toString();

    }
}
