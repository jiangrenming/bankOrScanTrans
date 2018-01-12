package com.nld.cloudpos.ecash.activity;

import android.content.Intent;

import com.nld.cloudpos.payment.base.BaseInputMoneyActivity;


/**
 * @description 指定账户圈存
 * @date 2015-11-3 13:58:36
 * @author Xrh
 */
public class AppointLoadInput extends BaseInputMoneyActivity {

	@Override
	public Intent getNextStep() {
		return new Intent( this,AppointLoadPasswd.class);
	}

	@Override
	public void initViewData() {
		setTopTitle("指定账户圈存");
	}
}
