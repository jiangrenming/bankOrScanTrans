package com.nld.starpos.wxtrade.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.nld.starpos.wxtrade.R;
import com.nld.starpos.wxtrade.bean.scan_pay.MessageTipBean;
import com.nld.starpos.wxtrade.utils.MessageUtils;

/**
 * Created by jiangrenming on 2017/9/26.
 * 扫码等待授权支付的界面
 */

public class MessageTipActivity extends Activity{

    private MessageTipBean bean;
    /**
     * Title
     */
    private String title;

    /**
     * 引导信息(请输入流水号)
     */
    private String content;

    /**
     * 能否取消
     */
    private boolean canCancel = false;

    private Dialog msg_tip_dialog;

    View.OnClickListener listenerSure = null;

    View.OnClickListener listenerCancel = null;

    View.OnClickListener listenerClose = null;

    // 是否显示右上角的关闭按钮。
    private boolean isShowIcon;
    // 防止重复走onStart
    private boolean isSecond;



    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_view);
        initData();

    }

    private void initData() {
        bean = (MessageTipBean) getIntent().getSerializableExtra("message");
        if (null != bean){
            title = bean.getTitle();
            content = bean.getContent();
            canCancel = bean.isCancelable();
            isShowIcon = bean.isShowIcon();
            bean.setResult(false);
            Log.i("TAG","消息"+bean.getTitle());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isSecond) {
            listenerSure = new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    bean.setResult(true);
                    onSuccess();
                }
            };
            listenerCancel = null;
            if (canCancel) {
                listenerCancel = new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        bean.setResult(false);
                        onSuccess();
                    }
                };
            }
            listenerClose = null;
            if (isShowIcon) {
                listenerClose = new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        bean.setResult(false);
                        onSuccess();
                    }
                };
            }
            msg_tip_dialog = MessageUtils.showCommonDialog(MessageTipActivity.this, title, content, listenerSure, listenerCancel, listenerClose);
        }
        isSecond = true;
    }


    private void onSuccess() {
        try{
            if (msg_tip_dialog != null) {
                msg_tip_dialog.dismiss();
                msg_tip_dialog = null;
                Intent i = new Intent(MessageTipActivity.this,StartScanActivity.class);
                i.putExtra("message",bean);
                setResult(RESULT_OK,i);
                finish();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
