package com.nld.cloudpos.ecash.activity;

import android.content.Intent;

import com.nld.cloudpos.payment.base.BaseSwipeCardActivity;


/**
 * @description 指定账户圈存
 * @date 2015-10-30 20:21:44
 * @author Xrh
 */
public class AppointLoadCard extends BaseSwipeCardActivity {

	@Override
	public void initViewData() {
		setTopTitle("指定账户圈存");
		mCarnoTv.setHint("请插卡");
	}

	@Override
	public Intent getNextStep() {
		Intent it = new Intent(mContext, AppointLoadInput.class);
		return it;
	}
}
