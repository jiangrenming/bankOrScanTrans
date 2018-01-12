package com.nld.cloudpos.payment.controller;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.base.BaseAbstractActivity;

import common.StringUtil;

/**
 * Created by jiangrenming on 2017/12/4.
 * 预授权完成界面<输入卡号等操作></>
 */

public class PreCompleterSubmitActivity extends BaseAbstractActivity {

    @ViewInject(R.id.et_pan)
    EditText et_pan;
    @ViewInject(R.id.tv_card_enter)
    TextView tv_card_enter;

    @Override
    public int contentViewSourceID() {
        return R.layout.pre_complete_layout;
    }

    @Override
    public void initView() {
        setTopTitle("预授权完成请求");
        ViewUtils.inject(this);
        et_pan.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int action, KeyEvent event) {
                if (action == EditorInfo.IME_ACTION_DONE ||
                        action == EditorInfo.IME_ACTION_SEND ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String vaule = et_pan.getText().toString().trim();
                    if (StringUtil.isEmpty(vaule) || vaule.length() < 13) {
                        return true;
                    }
//                    cancelCardReader();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onServiceConnecteSuccess(AidlDeviceService service) {}

    @Override
    public void onServiceBindFaild() {}

    @Override
    public boolean saveValue() {
        return false;
    }
}
