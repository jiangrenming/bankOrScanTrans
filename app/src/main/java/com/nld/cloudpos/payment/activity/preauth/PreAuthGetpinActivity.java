/**
 * 
 */
package com.nld.cloudpos.payment.activity.preauth;

import android.content.Intent;

import com.nld.cloudpos.payment.base.BaseGetPinActivity;
import com.nld.starpos.banktrade.activity.StartTransActivity;

/**
 * @author lin 2015年8月31日
 */
public class PreAuthGetpinActivity extends BaseGetPinActivity {

	@Override
	public void initViewData() {
		setTopTitle("预授权");
	}

	@Override
	public Intent getNextStep() {
	 //   Intent it=new Intent(mContext,TransStartActivity.class);
      //  it.putExtra(TransStartActivity.TRANS_NEXT_ACTIVITY_TAG, PrintResultActivity.class.getName());
		Intent intent = new Intent(mContext, StartTransActivity.class);
        return intent;
	}
}
