/**
 * 
 */
package com.nld.cloudpos.payment.activity.preauth;

import android.content.Intent;

import com.nld.cloudpos.payment.base.BaseSwipeCardActivity;


/**
 * @author lin 2015年8月31日
 */
public class PreAuthCompleteSubmitActivity extends BaseSwipeCardActivity {

	@Override
	public void initViewData() {
		setTopTitle("预授权完成");
		showTransMoney(false);
		showInputCarno(true);
	}

	@Override
	public Intent getNextStep() {
		return new Intent(PreAuthCompleteSubmitActivity.this,
				PreAuthCompleteInputAuthActivity.class);
	}

}
