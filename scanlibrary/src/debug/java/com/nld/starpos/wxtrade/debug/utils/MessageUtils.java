package com.nld.starpos.wxtrade.debug.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.View.OnClickListener;

import com.nld.starpos.wxtrade.R;

public class MessageUtils {

    public static Dialog showCommonDialog(Context context, String title,
                                          CharSequence message,
                                          OnClickListener listenerSure,
                                          OnClickListener listenerCancel, OnClickListener listenerClose) {
        return showCommonDialog(context, title, message, R.string.common_sure, -1,
                R.string.common_cancle, listenerSure, null, listenerCancel, listenerClose);

    }
    public static CommonDialog showCommonDialog(final Context context, String title,
                                                CharSequence message, int resSureText, int resMiddelText, int resCancelText,
                                                OnClickListener listenerSure, OnClickListener listenerMiddle,
                                                OnClickListener listenerCancel, OnClickListener listenerClose) {
        return showCommonDialog(context, title,
                message, resSureText, resMiddelText, resCancelText,
                listenerSure, listenerMiddle,
                listenerCancel,
                -1, CommonDialog.TimeOutOper.NONE, listenerClose);
    }

    public static CommonDialog showCommonDialog(final Context context, String title,
                                                CharSequence message, int resSureText, int resMiddelText, int resCancelText,
                                                OnClickListener listenerSure, OnClickListener listenerMiddle,
                                                OnClickListener listenerCancel,
                                                int timeOut, CommonDialog.TimeOutOper timeOutOper, OnClickListener listenerClose) {
        final CommonDialog commonDialog = getCommonDialog(context, title, message, resSureText, resMiddelText, resCancelText, listenerSure, listenerMiddle, listenerCancel, timeOut, timeOutOper, listenerClose);
        commonDialog.show();
        return commonDialog;
    }
    public static CommonDialog getCommonDialog(Context context, String title,
                                               CharSequence message, int resSureText, int resMiddelText, int resCancelText,
                                               OnClickListener listenerSure, OnClickListener listenerMiddle,
                                               OnClickListener listenerCancel, int timeOut, CommonDialog.TimeOutOper timeOutOper, OnClickListener listenerClose) {

        final CommonDialog commonDialog = new CommonDialog(context, timeOut, timeOutOper);
        if (title != null) {
            commonDialog.setTitle(title);
        }
        commonDialog.setContent(message.toString());
        commonDialog.setSureListener(resSureText, listenerSure);
        commonDialog.setCancelListener(resCancelText, listenerCancel);
        commonDialog.setMiddleListener(resMiddelText, listenerMiddle);
        commonDialog.setIconListener(listenerClose);

        return commonDialog;
    }
}
