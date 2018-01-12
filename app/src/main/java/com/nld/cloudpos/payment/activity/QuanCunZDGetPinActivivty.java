package com.nld.cloudpos.payment.activity;

import android.content.Intent;

import com.nld.cloudpos.payment.base.BaseGetPinActivity;

public class QuanCunZDGetPinActivivty extends BaseGetPinActivity {

    @Override
    public void initViewData() {
        setTopTitle("指定账户圈存");
    }

    @Override
    public Intent getNextStep() {
        Intent it=new Intent(mContext,TransStartActivity.class);
        it.putExtra(TransStartActivity.TRANS_NEXT_ACTIVITY_TAG, PrintResultActivity.class);
        return it;
    }

}
