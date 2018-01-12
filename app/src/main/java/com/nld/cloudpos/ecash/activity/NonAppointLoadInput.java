package com.nld.cloudpos.ecash.activity;

import android.content.Intent;

import com.nld.cloudpos.payment.base.BaseInputMoneyActivity;


/**
 * @description 非指定账户圈存
 * @date 2015-11-10 20:19:28
 * @author Xrh
 */
public class NonAppointLoadInput extends BaseInputMoneyActivity {

	@Override
	public Intent getNextStep() {
		return null;
	}

	@Override
	public void initViewData() {
		setTopTitle("非指定账户圈存");
	}
}
