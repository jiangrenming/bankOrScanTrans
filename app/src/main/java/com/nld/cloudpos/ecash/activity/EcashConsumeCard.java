package com.nld.cloudpos.ecash.activity;

import android.content.Intent;

import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.base.BaseSwipeCardActivity;

public class EcashConsumeCard extends BaseSwipeCardActivity {

	@Override
	public void initViewData() {
		setTopTitle("普通消费");
		mCarnoTv.setText("请插卡");
		iv_bottom.setImageResource(R.drawable.pic_1_2);
	}

	@Override
	public Intent getNextStep() {
        return new Intent(this, EcashConsumePasswd.class);
	}
}
