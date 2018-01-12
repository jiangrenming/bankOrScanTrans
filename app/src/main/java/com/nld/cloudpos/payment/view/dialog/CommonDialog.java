package com.nld.cloudpos.payment.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.nld.cloudpos.bankline.R;

public class CommonDialog extends Dialog {
    private TextView titleView, tvAlertMsg;
    private Button bt_alert;
    private FrameLayout fl_dialog;

    public CommonDialog(Context context, int timeOut, com.nld.cloudpos.payment.controller.CommonDialog.TimeOutOper timeOutOper) {
        super(context, R.style.dialog);
        setContentView(R.layout.layout_common_dialog);
        titleView = (TextView) findViewById(R.id.tvAlertTitle);
        fl_dialog = (FrameLayout) findViewById(R.id.fl_dialog);
        bt_alert = (Button) findViewById(R.id.bt_alert);
        tvAlertMsg = (TextView) findViewById(R.id.tvAlertMsg);
    }

    public void setTitle(int resId) {
        titleView.setText(resId);
    }

    public void setTitle(String title) {
        titleView.setText(title);
    }

    public void setNoTitle() {
        titleView.setVisibility(View.GONE);
    }

    public void setMessage(String msg) {
        tvAlertMsg.setText(msg);
        tvAlertMsg.setVisibility(View.VISIBLE);
    }

    public void setMessage(int res, int str) {
        tvAlertMsg.setCompoundDrawablesWithIntrinsicBounds(0, res, 0, 0);
        tvAlertMsg.setText(str);
        tvAlertMsg.setVisibility(View.VISIBLE);
    }

    public void addView(View view) {
        fl_dialog.addView(view);
    }

    public Button getPositiveButton() {
        return bt_alert;
    }

    public void setNoPositiveButton() {
        bt_alert.setVisibility(View.GONE);
    }

    public void setPositiveButton(int strID, View.OnClickListener listener) {
        bt_alert.setVisibility(View.VISIBLE);
        bt_alert.setOnClickListener(listener);
        if (bt_alert != null) {
            bt_alert.setText(strID);
        }
    }

    public void setPositiveButton(String str, View.OnClickListener listener) {
        bt_alert.setVisibility(View.VISIBLE);
        bt_alert.setOnClickListener(listener);
        if (bt_alert != null) {
            bt_alert.setText(str);
        }
    }
}