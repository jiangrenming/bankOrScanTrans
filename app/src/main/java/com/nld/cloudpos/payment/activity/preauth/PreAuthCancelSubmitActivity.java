/**
 *
 */
package com.nld.cloudpos.payment.activity.preauth;

import android.content.Intent;

import com.nld.cloudpos.payment.base.BaseSwipeCardActivity;


/**
 *  预授权撤销
 */
public class PreAuthCancelSubmitActivity extends BaseSwipeCardActivity {

    @Override
    public void initViewData() {
        setTopTitle("预授权撤销");
        showTransMoney(false);
        showInputCarno(false);
    }

    @Override
    public Intent getNextStep() {
        Intent it = new Intent(this, PreAuthCancelInputAuthActivity.class);
        return it;
    }
}
