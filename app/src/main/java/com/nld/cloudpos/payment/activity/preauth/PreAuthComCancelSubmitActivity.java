/**
 * 
 */
package com.nld.cloudpos.payment.activity.preauth;

import android.content.Intent;
import com.nld.cloudpos.payment.base.BaseSwipeCardActivity;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.TransConstans;

/**
 * @author lin 2015年9月1日
 */
public class PreAuthComCancelSubmitActivity extends BaseSwipeCardActivity {

	@Override
	public void initViewData() {
        if(Cache.getInstance().getTransCode().equals(TransConstans.TRANS_CODE_CONSUME_CX)){
            setTitle("消费撤销");
        }else if(Cache.getInstance().getTransCode().equals(TransConstans.TRANS_CODE_PRE_COMPLET_CX)){
            setTitle("预授权完成撤销");
        }
        showTransMoney(true);
        showInputCarno(false);
	}

	@Override
	public Intent getNextStep() {
		return new Intent(PreAuthComCancelSubmitActivity.this,
				PreAuthComCancelGetpinActivity.class);
	}
}
