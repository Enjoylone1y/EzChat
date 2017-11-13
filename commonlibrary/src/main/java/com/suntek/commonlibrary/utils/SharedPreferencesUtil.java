package com.suntek.commonlibrary.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class SharedPreferencesUtil {


    public static void setIntSharedPreferences(Context context, String name, String key, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void setBooleanSharedPreferences(Context context, String name, String key, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void setFloatSharedPreferences(Context context, String name, String key, float value) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public static void setStringSharedPreferences(Context context, String name, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.contains(key)) {
            editor.remove(key);
            editor.commit();
        }
        editor.putString(key, value);
        editor.commit();
    }

    public static void setLongSharedPreferences(Context context, String name, String key, long value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static void setStringSetSharedPreferences(Context context, String name, String key, Set<String> value) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(key, value);
        editor.commit();
    }

    public static int getIntSharedPreferences(Context context, String name, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        int value = sharedPreferences.getInt(key, 0);
        return value;
    }

    public static boolean getBooleanSharedPreferences(Context context, String name, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        boolean value = sharedPreferences.getBoolean(key, true);
        return value;
    }

    public static float getFloatSharedPreferences(Context context, String name, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        float value = sharedPreferences.getFloat(key, 0);
        return value;
    }

    public static String getStringSharedPreferences(Context context, String name, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        String value = sharedPreferences.getString(key, "");
        return value;
    }

    public static long getLongSharedPreferences(Context context, String name, String key) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        long value = sharedPreferences.getLong(key, 0);
        return value;
    }

    public static Set<String> getStringSetSharedPreferences(Context context, String name, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
        Set<String> value = sharedPreferences.getStringSet(key, null);
        return value;
    }


}
