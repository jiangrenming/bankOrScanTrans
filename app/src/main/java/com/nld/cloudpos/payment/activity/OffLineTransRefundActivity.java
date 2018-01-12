package com.nld.cloudpos.payment.activity;


import android.content.Intent;

import com.nld.starpos.banktrade.activity.StartTransActivity;


/**
 *
 * @author jiangrenming
 * @date 2017/12/14
 * 脱机退货
 */

public class OffLineTransRefundActivity extends BaseOffLineRefundActivity {


    @Override
    public Intent goNextStep() {
        Intent intent=new Intent(mContext,StartTransActivity.class);
        return intent;
    }
}
