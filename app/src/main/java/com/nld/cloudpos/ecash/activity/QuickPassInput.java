package com.nld.cloudpos.ecash.activity;

import android.content.Intent;
import com.nld.cloudpos.payment.base.BaseInputMoneyActivity;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.TransConstans;

/**
 * @description 非接快速支付
 * @date 2015-10-30 20:21:44
 * @author Xrh
 */
public class QuickPassInput extends BaseInputMoneyActivity {

	@Override
	public Intent getNextStep() {
		return null;
	}

	@Override
	public void initViewData() {
		Cache.getInstance().clearAllData();
		setTopTitle("快速支付");
		Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_DZXJ_TRANS_QUICK_PAY);
	}


}
