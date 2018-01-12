package com.nld.cloudpos.ecash.activity;
import android.content.Intent;
import com.nld.cloudpos.payment.base.BaseGetPinActivity;
import com.nld.starpos.banktrade.activity.StartTransActivity;

public class AppointLoadPasswd extends BaseGetPinActivity {

	@Override
	public void initViewData() {
		setTopTitle("指定账户圈存");
	}

	@Override
	public Intent getNextStep() {
		Intent it = new Intent(this, StartTransActivity.class);
		return it;
	}
}
