package com.nld.starpos.wxtrade.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import common.DensityUtils;


@SuppressLint("ShowToast")
public final class ToastUtils {
    private static Toast mToast;
    private static int mDuration = Toast.LENGTH_SHORT;

    private static Context mContext;

    public static void init(Context context) {
        mContext = context;
        mToast = Toast.makeText(context, "", mDuration);
    }

    public static void showToast(String text) {
        showToast(text, Toast.LENGTH_SHORT);
    }

    public static void showToast(int resId) {
        try {
            if (resId > 0) {
                mToast.setText(resId);
                mToast.setDuration(Toast.LENGTH_SHORT);
                mToast.setGravity(Gravity.BOTTOM, 0, DensityUtils.dip2px(mContext, 60));
                if (((LinearLayout) mToast.getView()).getChildAt(0) instanceof ImageView) {
                    ((LinearLayout) mToast.getView()).removeViewAt(0);
                }
                mToast.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showToast(String text, int duration) {
        try {
            if (!TextUtils.isEmpty(text)) {
                mToast.setText(text);
                mToast.setDuration(duration);
                mToast.setGravity(Gravity.BOTTOM, 0, DensityUtils.dip2px(mContext, 60));
                if (((LinearLayout) mToast.getView()).getChildAt(0) instanceof ImageView) {
                    ((LinearLayout) mToast.getView()).removeViewAt(0);
                }
                mToast.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showToast(Activity activity, String text, int iconResId, int type) {
        try {
            if (!TextUtils.isEmpty(text)) {
                mToast.setText(text);
                mToast.setDuration(type);
                if (iconResId > 0) {
                    mToast.setGravity(Gravity.CENTER, 0, 0);
                    LinearLayout toastView = (LinearLayout) mToast.getView();
                    if (toastView.getChildAt(0) instanceof ImageView) {
                        ((ImageView) toastView.getChildAt(0)).setImageResource(iconResId);
                    } else {
                        ImageView toastImageView = new ImageView(activity);
                        toastImageView.setImageResource(iconResId);
                        toastView.addView(toastImageView, 0);
                    }
                } else {
                    mToast.setGravity(Gravity.BOTTOM, 0, DensityUtils.dip2px(activity, 60));
                    if (((LinearLayout) mToast.getView()).getChildAt(0) instanceof ImageView) {
                        ((LinearLayout) mToast.getView()).removeViewAt(0);
                    }
                }
                mToast.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}