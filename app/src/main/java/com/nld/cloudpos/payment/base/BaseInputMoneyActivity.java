package com.nld.cloudpos.payment.base;


import android.content.Intent;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.wxtrade.utils.ToastUtils;

import java.text.DecimalFormat;

import common.StringUtil;

public abstract class BaseInputMoneyActivity extends BaseAbstractActivity implements OnClickListener {

    private EditText input_money;
    private TextView enter;

    
    @Override
    public int contentViewSourceID() {
        return R.layout.consume_form;
    }

    @Override
    public void initView() {
        initViewData();
        setTopDefaultReturn();
        input_money = (EditText) findViewById(R.id.et_money);
        enter = (TextView) findViewById(R.id.tv_authsale_enter);
        enter.setOnClickListener(this);

        input_money.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int action,
                                          KeyEvent event) {
                if (action == EditorInfo.IME_ACTION_DONE
                        || action == EditorInfo.IME_ACTION_SEND
                        || action == EditorInfo.IME_ACTION_NEXT
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    if (done()){
                        Intent nextStep = getNextStep();
                        goToNextActivity(nextStep);
                    }
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
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_authsale_enter:
                if (done()){
                    Intent nextStep = getNextStep();
                    goToNextActivity(nextStep);
                }
                break;
            default:
                break;
        }
    }

    public  boolean done(){
        String amt = input_money.getText().toString().trim();
        if (StringUtil.isEmpty(amt)|| TextUtils.equals(amt, "0.00")) {
            ToastUtils.showToast("请输入金额");
            return  false;
        }
        String money = amt.replace(".", "").trim();
        if (Long.valueOf(money) / 100 >= 100000000){
            ToastUtils.showToast("金额输入超限！");
            return false;
        }
        try{
            DecimalFormat decfmat = new DecimalFormat("#######0.00");
            Cache.getInstance().setTransMoney(decfmat.format(Double.valueOf(amt)));
        }catch (Exception e){
            e.printStackTrace();
            ToastUtils.showToast("金额输入有误！");
            return false;
        }
        return  true;
    }

    public abstract Intent getNextStep();
    public abstract void initViewData();
}
