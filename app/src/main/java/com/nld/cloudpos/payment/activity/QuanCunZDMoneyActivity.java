package com.nld.cloudpos.payment.activity;

import android.content.Intent;

import com.nld.cloudpos.payment.base.BaseInputMoneyActivity;


public class QuanCunZDMoneyActivity extends BaseInputMoneyActivity {

    @Override
    public Intent getNextStep() {
        return null;
    }

    @Override
    public void initViewData() {
        setTopTitle("指定账户圈存");
    }



}
