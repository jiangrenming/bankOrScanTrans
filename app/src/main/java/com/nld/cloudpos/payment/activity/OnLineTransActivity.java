package com.nld.cloudpos.payment.activity;

import android.content.Intent;

import com.nld.cloudpos.payment.activity.preauth.BasePreCompleteActivity;
import com.nld.starpos.banktrade.activity.StartTransActivity;

/**
 * 联机退货
 */
public class OnLineTransActivity extends BasePreCompleteActivity {

    @Override
    public Intent goNextStep() {
       Intent intent=new Intent(mContext,StartTransActivity.class);
        return intent;
    }
}
