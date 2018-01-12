package com.nld.cloudpos.ecash.activity;

import android.content.Intent;

import com.nld.cloudpos.payment.base.BaseGetPinActivity;


public class NonAppointLoadPasswd extends BaseGetPinActivity {

	@Override
	public void initViewData() {
		setTopTitle("非指定账户圈存");
	}

	@Override
	public Intent getNextStep() {
		Intent it = new Intent(this, NonAppointLoadCardIn.class);
		return it;
	}
}
