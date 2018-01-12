package com.nld.starpos.wxtrade.debug.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jiangrenming on 2017/11/2.
 */

public class ShareScanPreferenceUtils {

    private static final String SHARE_PREFS_NAME = "com.android.xdl.cloudpos";
    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(SHARE_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void putBoolean(Context context,String key, boolean value) {
        getPreferences(context).edit().putBoolean(key, value).commit();
    }

    public static boolean getBoolean(Context context,String key, boolean defaultValue) {
        return getPreferences(context).getBoolean(key, defaultValue);
    }

    public static void putString(Context context,String key, String value) {
        getPreferences(context).edit().putString(key, value).commit();
    }

    public static String getString(Context context,String key, String defaultValue) {
        return getPreferences(context).getString(key, defaultValue);
    }

    public static void putInt(Context context,String key, int value) {
        getPreferences(context).edit().putInt(key, value).commit();
    }

    public static int getInt(Context context,String key, int defaultValue) {
        return getPreferences(context).getInt(key, defaultValue);
    }
    public  static void clearData(Context context,String key){
        SharedPreferences.Editor edit = getPreferences(context).edit();
        edit.remove(key);
        edit.commit();
    }
}
