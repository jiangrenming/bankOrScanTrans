package com.nld.cloudpos.ecash.activity;

import android.content.Intent;
import com.nld.cloudpos.payment.base.BaseInputMoneyActivity;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.TransConstans;

/**
 * @description 电子现金消费
 * @date 2015-10-30 20:21:44
 * @author Xrh
 */
public class EcashConsumeInput extends BaseInputMoneyActivity {

	@Override
	public Intent getNextStep() {
		return null;
	}

	@Override
	public void initViewData() {
		Cache.getInstance().clearAllData();
		setTopTitle("普通消费");
		Cache.getInstance().setTransCode(
				TransConstans.TRANS_CODE_DZXJ_TRANS_PUTONG_CONSUMER);
	}


}
