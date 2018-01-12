package com.nld.cloudpos.ecash.activity;

import android.content.Intent;
import com.nld.cloudpos.payment.base.BaseGetPinActivity;
import com.nld.starpos.banktrade.activity.StartTransActivity;

/**
 * @description 电子现金消费流程，输入联机PIN
 * @date 2015-11-3 11:05:55
 * @author Xrh
 */
public class EcashConsumePasswd extends BaseGetPinActivity {
	@Override
	public void initViewData() {
		setTopTitle("普通消费");
	}
	
	@Override
	public Intent getNextStep() {
		Intent it = new Intent(this, StartTransActivity.class);
		return it;
	}
}
