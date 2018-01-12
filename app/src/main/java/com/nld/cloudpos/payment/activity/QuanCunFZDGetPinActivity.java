package com.nld.cloudpos.payment.activity;

import android.content.Intent;

import com.nld.cloudpos.payment.base.BaseGetPinActivity;

/**
 * 非指定账户圈存输密
 * @author Administrator
 *
 */
public class QuanCunFZDGetPinActivity extends BaseGetPinActivity {

    @Override
    public void initViewData() {
        setTopTitle("非指定账户圈存");
    }

    @Override
    public Intent getNextStep() {
        Intent it=new Intent(mContext,QuanCunFZDSwipeInActivity.class);
        return it;
    }

}
