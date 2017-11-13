package com.ezreal.timeselectview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class CityPickerView extends LinearLayout implements View.OnClickListener{

    private WheelView mProvincePicker;
    private WheelView mCityPicker;

    private int mCurrProvinceIndex = -1;
    private int mCurrCityIndex = -1;

    private AreaDataUtil mAreaDataUtil;
    private ArrayList<String> mProvinceList = new ArrayList<>();
    private TextView mTvConfirm;
    private OnCitySelectedListener mSelectedListener;
    public CityPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getAreaInfo();
    }

    public CityPickerView(Context context) {
        this(context, null);
    }

    private void getAreaInfo() {
        mAreaDataUtil = new AreaDataUtil(getContext());
        mProvinceList = mAreaDataUtil.getProvinces();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_btn_confirm){
            if (mSelectedListener != null){
                mSelectedListener.citySelected(getProvince(),getCity());
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater.from(getContext()).inflate(R.layout.layout_city_picker, this);
        mTvConfirm = (TextView) findViewById(R.id.tv_btn_confirm);
        mTvConfirm.setOnClickListener(this);
        mProvincePicker = (WheelView) findViewById(R.id.province_wv);
        mCityPicker = (WheelView) findViewById(R.id.city_wv);

        mProvincePicker.setData(mProvinceList);
        mProvincePicker.setDefault(0);

        String defaultProvince = mProvinceList.get(0);
        mCityPicker.setData(mAreaDataUtil.getCityByProvince(defaultProvince));
        mCityPicker.setDefault(1);

        mProvincePicker.setOnSelectListener(new WheelView.OnSelectListener() {
            @Override
            public void endSelect(int id, String text) {
                if (text.equals("") || text == null)
                    return;
                if (mCurrProvinceIndex != id) {
                    mCurrProvinceIndex = id;
                    String selectProvince = mProvincePicker.getSelectedText();
                    if (selectProvince == null || selectProvince.equals(""))
                        return;

                    // get city names by province
                    ArrayList<String> city = mAreaDataUtil.getCityByProvince(mProvinceList.get(id));
                    if (city.size() == 0) {
                        return;
                    }

                    mCityPicker.setData(city);

                    if (city.size() > 1) {
                        //if city is more than one,show start index == 1
                        mCityPicker.setDefault(1);
                    } else {
                        mCityPicker.setDefault(0);
                    }
                }

            }

            @Override
            public void selecting(int id, String text) {
            }
        });

        mCityPicker.setOnSelectListener(new WheelView.OnSelectListener() {

            @Override
            public void endSelect(int id, String text) {
                if (text.equals("") || text == null)
                    return;
                if (mCurrCityIndex != id) {
                    mCurrCityIndex = id;
                    String selectCity = mCityPicker.getSelectedText();
                    if (selectCity == null || selectCity.equals(""))
                        return;
                    int lastIndex = Integer.valueOf(mCityPicker.getListSize());
                    if (id > lastIndex) {
                        mCityPicker.setDefault(lastIndex - 1);
                    }
                }
            }

            @Override
            public void selecting(int id, String text) {

            }
        });
    }

    public String getProvince() {
        if (mProvincePicker == null) {
            return null;
        }
        return mProvincePicker.getSelectedText();
    }

    public String getCity() {
        if (mCityPicker == null) {
            return null;
        }
        return mCityPicker.getSelectedText();
    }

    public void setCitySelectedListener(OnCitySelectedListener listener){
        this.mSelectedListener = listener;
    }

    public interface OnCitySelectedListener{
        void citySelected(String province,String city);
    }


}
