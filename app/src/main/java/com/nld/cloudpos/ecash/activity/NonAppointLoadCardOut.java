package com.nld.cloudpos.ecash.activity;

import android.content.Intent;

import com.nld.cloudpos.payment.base.BaseSwipeCardActivity;


/**
 * @description 非指定账户圈存
 * @date 2015-11-10 20:09:12
 * @author Xrh
 */
public class NonAppointLoadCardOut extends BaseSwipeCardActivity {

	@Override
	public void initViewData() {
		setTopTitle("非指定账户圈存");
		showTransMoney(false);
		mCarnoTv.setHint("请刷或插卡（转出卡）");
		mCarnoTipTV.setText("请刷或插卡（转出卡）");
		isInCard = false;
	}

	@Override
	public Intent getNextStep() {
		Intent it = new Intent(this, NonAppointLoadInput.class);
		return it;
	}
}
