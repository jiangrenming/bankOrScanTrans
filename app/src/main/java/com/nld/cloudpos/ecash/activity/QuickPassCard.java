package com.nld.cloudpos.ecash.activity;

import android.content.Intent;

import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.activity.PrintResultActivity;
import com.nld.cloudpos.util.MyLog;

/**
 * @description 非接快速支付
 * @date 2015-10-30 20:59:41
 * @author Xrh
 */
public class QuickPassCard extends PbocSimpleProcess {

	private final MyLog logger = MyLog.getLogger(this.getClass());

	@Override
	public void initViewData() {
		setTopTitle("快速支付");
		mCarnoTv.setText("请挥卡");
		iv_bottom.setImageResource(R.drawable.pic_1_3);
	}

	@Override
	public Intent getNextStep() {
		return new Intent(this, PrintResultActivity.class);
	}

	@Override
	public Intent getNextOnline() {
		return new Intent(this, QuickPassPasswd.class);
	}

}
