package com.nld.cloudpos.ecash.activity;

import android.content.Intent;

import com.nld.cloudpos.payment.activity.PrintResultActivity;
import com.nld.cloudpos.payment.activity.TransStartActivity;
import com.nld.cloudpos.payment.base.BaseGetPinActivity;

/**
 * @description 电子现金快速支付，输入联机PIN
 * @date 2015-11-3 11:05:55
 * @author Xrh
 */
public class QuickPassPasswd extends BaseGetPinActivity {
	@Override
	public void initViewData() {
		setTopTitle("快速支付");
	}

	@Override
	public Intent getNextStep() {
		Intent it = new Intent(this, TransStartActivity.class);
		it.putExtra(TransStartActivity.TRANS_NEXT_ACTIVITY_TAG,
				PrintResultActivity.class.getName());
		return it;
	}
}
