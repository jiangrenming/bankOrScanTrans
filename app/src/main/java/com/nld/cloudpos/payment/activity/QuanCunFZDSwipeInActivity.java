package com.nld.cloudpos.payment.activity;

import android.content.Intent;
import com.nld.cloudpos.payment.base.BaseSwipeCardActivity;
import com.nld.starpos.banktrade.activity.StartTransActivity;

/**
 * 非指定账户圈存转入卡
 * @author Administrator
 *
 */
public class QuanCunFZDSwipeInActivity extends BaseSwipeCardActivity {

    @Override
    public void initViewData() {
        setTopTitle("非指定账户圈存");
        showTransMoney(false);
        mCarnoTv.setHint("请插卡（转入卡）");
        mCarnoTipTV.setText("请插卡（转入卡）");
        isInCard=true;
    }

    @Override
    public Intent getNextStep() {
        Intent it = new Intent(this, StartTransActivity.class);
        return it;
    }

}
