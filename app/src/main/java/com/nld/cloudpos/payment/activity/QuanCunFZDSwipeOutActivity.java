package com.nld.cloudpos.payment.activity;

import android.content.Intent;

import com.nld.cloudpos.payment.base.BaseSwipeCardActivity;


/**
 * 非指定圈存转出卡刷卡
 */
public class QuanCunFZDSwipeOutActivity extends BaseSwipeCardActivity {

    @Override
    public void initViewData() {
        setTopTitle("非指定账户圈存");
        showTransMoney(false);
        mCarnoTv.setHint("请刷或插卡（转出卡）");
        mCarnoTipTV.setText("请刷或插卡（转出卡）");
        isInCard=false;
    }

    @Override
    public Intent getNextStep() {
        Intent intent=new Intent(mContext,QuanCunFZDMoneyActivity.class);
        return intent;
    }

}
