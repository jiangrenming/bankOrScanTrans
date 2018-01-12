package com.nld.cloudpos.payment.activity;

import android.content.Intent;

import com.nld.cloudpos.payment.base.BaseSwipeCardActivity;


public class OnLineSwipeCardActivity extends BaseSwipeCardActivity {

    @Override
    public void initViewData() {
        setTopTitle("退货");
        showTransMoney(false);
    }

    @Override
    public Intent getNextStep() {
        Intent it=new Intent(mContext,OnLineTransActivity.class);
        return it;
    }

}
