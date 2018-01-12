/**
 * 
 */
package com.nld.cloudpos.payment.activity.preauth;

import android.content.Intent;
import com.nld.cloudpos.payment.activity.ConsumeGetpinActivity;
import com.nld.starpos.banktrade.activity.StartTransActivity;
import com.nld.starpos.banktrade.utils.ParamsConts;
import com.nld.starpos.banktrade.utils.ShareBankPreferenceUtils;

/**
 * 预授权完成请求界面跳转<分需要密码和不需输密码></>
 */
public class PreAuthCompleteInputAuthActivity extends BasePreCompleteActivity {

	@Override
	public Intent goNextStep() {
		boolean pre_complete = ShareBankPreferenceUtils.getBoolean(ParamsConts.PARAMS_KEY_IS_AUTH_SALE_PIN, true);
		Intent intent= null;
		if (pre_complete){  //需要输入密码的时候
			intent = new Intent(mContext,ConsumeGetpinActivity.class);
			return intent;
		}else {
		//	intent=new Intent(mContext,TransStartActivity.class);
		//	intent.putExtra(TransStartActivity.TRANS_NEXT_ACTIVITY_TAG, PrintResultActivity.class.getName());
			intent=new Intent(mContext,StartTransActivity.class);
			return intent;
		}
	}
}
