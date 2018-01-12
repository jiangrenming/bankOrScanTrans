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
 *
 *预授权撤销跳转界面
 */
public class PreAuthCancelInputAuthActivity extends BasePreCompleteActivity {

	@Override
	public Intent goNextStep() {
		boolean isAuthVoidInputPwd = ShareBankPreferenceUtils.getBoolean(ParamsConts.PARAMS_KEY_IS_INPUT_AUTH_VOID,true);
        Intent intent = null;
        if (isAuthVoidInputPwd){  //需要输密
            intent = new Intent(mContext, ConsumeGetpinActivity.class);
            return  intent;
        }else {
         //   intent = new Intent(mContext, TransStartActivity.class);
          //  intent.putExtra(TransStartActivity.TRANS_NEXT_ACTIVITY_TAG, PrintResultActivity.class.getName());
            intent = new Intent(mContext, StartTransActivity.class);
            return  intent;
        }
	}
}
