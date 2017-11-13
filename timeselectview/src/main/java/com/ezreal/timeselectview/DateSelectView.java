package com.ezreal.timeselectview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by wudeng on 2017/8/8.
 */

public class DateSelectView extends LinearLayout{

    private WheelView mWvYear;
    private WheelView mWvMonth;
    private WheelView mWvDay;



    public DateSelectView(Context context) {
        this(context,null);
    }

    public DateSelectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
        initDate();
        initListener();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

    }

    private void initView(){
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View rootView = layoutInflater.inflate(R.layout.view_time_picker, this);
        mWvYear = (WheelView) rootView.findViewById(R.id.wv_year);
        mWvMonth = (WheelView) rootView.findViewById(R.id.wv_month);
        mWvDay = (WheelView) rootView.findViewById(R.id.wv_day);
    }

    private void initDate(){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String[] split = sdf.format(new Date()).split("-");
        int currentYear = Integer.parseInt(split[0]);
        int currentMonth = Integer.parseInt(split[1]);
        int currentDay = Integer.parseInt(split[2]);

        mWvYear.setData(getYearData(currentYear));
        mWvYear.setDefault(1);
        mWvMonth.setData(getMonthData());
        mWvMonth.setDefault(currentMonth - 1);
        mWvDay.setData(getDayData(getMaxDay(currentYear, currentMonth)));
        mWvDay.setDefault(currentDay - 1);
    }

    private void initListener(){
        mWvYear.setOnSelectListener(new WheelView.OnSelectListener() {
            @Override
            public void endSelect(int id, String text) {
                changeDayData();
            }

            @Override
            public void selecting(int id, String text) {

            }
        });

        mWvMonth.setOnSelectListener(new WheelView.OnSelectListener() {
            @Override
            public void endSelect(int id, String text) {
                changeDayData();
            }

            @Override
            public void selecting(int id, String text) {

            }
        });
    }

    private ArrayList<String> getYearData(int currentYear) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = currentYear + 1; i >= 1900; i--) {
            list.add(String.valueOf(i));
        }
        return list;
    }

    private ArrayList<String> getMonthData() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            list.add(String.valueOf(i));
        }
        return list;
    }

    private ArrayList<String> getDayData(int maxDay){
        ArrayList<String> list = new ArrayList<>();
        for (int i=1;i <= maxDay;i++){
            list.add(String.valueOf(i));
        }
        return list;
    }

    private int getMaxDay(int year,int month){
        if (month == 2){
            if (isLeapYear(year)){
                return 29;
            }else {
                return 28;
            }
        }else if (month == 1 || month == 3 || month == 5 || month == 7
                || month == 8 || month ==10 || month == 12){
            return 31;
        }else {
            return 30;
        }
    }

    private boolean isLeapYear(int year){
        return (year % 100 == 0 && year % 400 == 0)
                || (year % 100 != 0 && year % 4 == 0);
    }

    private void changeDayData(){
        int selectDay = getDay();
        int currentYear = getYear();
        int currentMonth = getMonth();
        int maxDay = getMaxDay(currentYear,currentMonth);

        mWvDay.setData(getDayData(maxDay));

        if (selectDay > maxDay){
            mWvDay.setDefault(maxDay - 1);
        }else {
            mWvDay.setDefault(selectDay - 1);
        }

    }

    public int getYear(){
        return Integer.parseInt(mWvYear.getSelectedText());
    }

    public int getMonth(){
        return Integer.parseInt(mWvMonth.getSelectedText());
    }

    public int getDay(){
        return Integer.parseInt(mWvDay.getSelectedText());
    }


}
