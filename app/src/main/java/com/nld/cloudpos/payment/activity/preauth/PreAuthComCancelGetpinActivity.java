/**
 * 
 */
package com.nld.cloudpos.payment.activity.preauth;

import android.content.Intent;
import com.nld.cloudpos.payment.base.BaseGetPinActivity;
import com.nld.starpos.banktrade.activity.StartTransActivity;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.TransConstans;

/**
 * 预授权完成撤销
 * @author lin
 * 2015年8月31日
 */
public class PreAuthComCancelGetpinActivity extends BaseGetPinActivity {

	@Override
	public void initViewData() {
        if(Cache.getInstance().getTransCode().equals(TransConstans.TRANS_CODE_CONSUME_CX)){
            setTitle("消费撤销");
        }else if(Cache.getInstance().getTransCode().equals(TransConstans.TRANS_CODE_CONSUME_CX)){
            setTitle("预授权完成撤销");
        }
		showMoneyView(false);
	}

	@Override
	public Intent getNextStep() {
	   // Intent it=new Intent(mContext,TransStartActivity.class);
     //   it.putExtra(TransStartActivity.TRANS_NEXT_ACTIVITY_TAG, PrintResultActivity.class.getName());
		Intent it=new Intent(mContext,StartTransActivity.class);
		return it;
	}
}
