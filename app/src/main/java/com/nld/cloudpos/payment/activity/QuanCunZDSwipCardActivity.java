package com.nld.cloudpos.payment.activity;

import android.content.Intent;

import com.nld.cloudpos.payment.base.BaseSwipeCardActivity;


public class QuanCunZDSwipCardActivity extends BaseSwipeCardActivity {

    @Override
    public void initViewData() {
        setTopTitle("指定账户圈存");
        showTransMoney(false);
        mCarnoTv.setHint("请插卡");
        mCarnoTipTV.setText("请插卡");
    }

    @Override
    public Intent getNextStep() {
        Intent it=new Intent(mContext, QuanCunZDMoneyActivity.class);
        return it;
    }

}
