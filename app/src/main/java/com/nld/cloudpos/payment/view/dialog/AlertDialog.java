package com.nld.cloudpos.payment.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nld.cloudpos.bankline.R;

public class AlertDialog extends Dialog {
    private TextView titleView, messageView, tvAlert;
    private LinearLayout content_container, ll_root;

    public AlertDialog(Context context, boolean b) {
        super(context, R.style.dialog);
        setCancelable(b);
        setContentView(R.layout.layout_dialog);
        titleView = (TextView) findViewById(R.id.tvAlertTitle);
        messageView = (TextView) findViewById(R.id.tvAlertMsg);
        ll_root = (LinearLayout) findViewById(R.id.ll_root);

        tvAlert = (TextView) findViewById(R.id.tvAlert);

        content_container = (LinearLayout) findViewById(R.id.content_container);
        content_container.setVisibility(View.GONE);
    }

    public void setTitle(int resId) {
        titleView.setText(resId);
    }

    public void setTitle(String title) {
        titleView.setText(title);
    }

    public void setMessage(int resId) {
        messageView.setText(resId);
    }

    public void setMessage(String message) {
        messageView.setText(message);
    }

    public void setMessageViewGravity(int gravity) {
        messageView.setGravity(gravity);
    }

    public void setBackgroundResource(int r) {
        ll_root.setBackgroundResource(r);
    }

    public void setMessagePadding(int left, int top, int right, int bottom) {
        messageView.setPadding(left, top, right, bottom);
    }

    public TextView getPositiveButton() {
        return tvAlert;
    }

    public void setPositiveButton(int strID, final View.OnClickListener listener) {
        content_container.setVisibility(View.VISIBLE);
        content_container.setOnClickListener(listener);
        if (tvAlert != null) {
            tvAlert.setText(strID);
        }
    }

    public void setPositiveButton(String str, final View.OnClickListener listener) {
        content_container.setVisibility(View.VISIBLE);
        content_container.setOnClickListener(listener);
        if (tvAlert != null) {
            tvAlert.setText(str);
        }
    }
}