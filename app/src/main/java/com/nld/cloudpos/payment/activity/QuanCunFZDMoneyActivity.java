package com.nld.cloudpos.payment.activity;

import android.content.Intent;

import com.nld.cloudpos.payment.base.BaseInputMoneyActivity;

/**
 * 非指定账户圈存，输入金额
 */
public class QuanCunFZDMoneyActivity extends BaseInputMoneyActivity {

    @Override
    public Intent getNextStep() {
        Intent it=new Intent(mContext,QuanCunFZDGetPinActivity.class);
        return it;
    }

    @Override
    public void initViewData() {
        setTopTitle("非指定账户圈存");
    }
}
