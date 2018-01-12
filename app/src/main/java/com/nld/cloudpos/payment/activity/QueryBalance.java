package com.nld.cloudpos.payment.activity;

import android.content.Intent;
import com.nld.cloudpos.payment.base.BaseSwipeCardActivity;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.TransConstans;

public class QueryBalance extends BaseSwipeCardActivity {

	@Override
	public void initViewData() {
		showTransMoney(false);
		showInputCarno(false);
		Cache.getInstance().clearAllData();
		Cache.getInstance().setTransCode(
				TransConstans.TRANS_CODE_QUERY_BALANCE);
		Cache.getInstance().setTransMoney("0000000010");
		setTopTitle("余额查询");

	}

	@Override
	public Intent getNextStep() {
		Intent it = new Intent(mContext, QueryBalanceGetPin.class);
		return it;
	}
}
