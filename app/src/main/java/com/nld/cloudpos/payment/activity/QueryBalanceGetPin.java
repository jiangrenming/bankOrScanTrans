package com.nld.cloudpos.payment.activity;

import android.content.Intent;

import com.nld.cloudpos.payment.base.BaseGetPinActivity;
import com.nld.starpos.banktrade.activity.StartTransActivity;

public class QueryBalanceGetPin extends BaseGetPinActivity {

    @Override
    public void initViewData() {
        setTopTitle("余额查询");
        showMoneyView(false);
    }

    @Override
    public Intent getNextStep() {
        Intent it = new Intent(this, StartTransActivity.class);
     //   it.putExtra(TransStartActivity.TRANS_NEXT_ACTIVITY_TAG, BalanceResultActivity.class.getName());
        return it;
    }
}
