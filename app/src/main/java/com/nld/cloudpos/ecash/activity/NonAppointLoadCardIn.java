package com.nld.cloudpos.ecash.activity;

import android.content.Intent;

import com.nld.cloudpos.payment.activity.PrintResultActivity;
import com.nld.cloudpos.payment.activity.TransStartActivity;


/**
 * @description 非指定账户圈存
 * @date 2015-11-10 20:09:12
 * @author Xrh
 */
public class NonAppointLoadCardIn extends PbocSimpleProcess {

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
		Intent it = new Intent(this, TransStartActivity.class);
		it.putExtra(TransStartActivity.TRANS_NEXT_ACTIVITY_TAG,
				PrintResultActivity.class.getName());
		return it;
	}

	@Override
	public Intent getNextOnline() {
		Intent it = new Intent(this, TransStartActivity.class);
		it.putExtra(TransStartActivity.TRANS_NEXT_ACTIVITY_TAG,
				PrintResultActivity.class.getName());
		return it;
	}
}
