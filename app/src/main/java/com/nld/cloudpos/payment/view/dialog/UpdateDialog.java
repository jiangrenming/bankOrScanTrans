package com.nld.cloudpos.payment.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nld.cloudpos.bankline.R;

public class UpdateDialog extends Dialog {
    private TextView titleView, messageView;
    private Button btAlert;
    private ImageView iv_cross;

    public UpdateDialog(Context context, boolean b) {
        super(context, R.style.dialog);
        setCancelable(b);
        setContentView(R.layout.layout_update_dialog);
        titleView = (TextView) findViewById(R.id.tvAlertTitle);
        messageView = (TextView) findViewById(R.id.tvAlertMsg);
        btAlert = (Button) findViewById(R.id.btAlert);
        iv_cross = (ImageView) findViewById(R.id.iv_cross);
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

    public void setMessagePadding(int left, int top, int right, int bottom) {
        messageView.setPadding(left, top, right, bottom);
    }

    public Button getPositiveButton() {
        return btAlert;
    }

    public void setPositiveButton(int strID, final View.OnClickListener listener) {
        if (btAlert != null) {
            btAlert.setText(strID);
            btAlert.setOnClickListener(listener);
        }
    }

    public void setCrossListener(View.OnClickListener listener) {
        if (iv_cross != null) {
            iv_cross.setOnClickListener(listener);
        }
    }

    public void setPositiveButton(String str, final View.OnClickListener listener) {
        if (btAlert != null) {
            btAlert.setText(str);
            btAlert.setOnClickListener(listener);
        }
    }

    public void setCrossVisibility(int visibility) {
        if (iv_cross != null) {
            iv_cross.setVisibility(visibility);
        }
    }
}