/**
 * 
 */
package com.nld.cloudpos.payment.activity.preauth;

import android.content.Intent;
import com.nld.cloudpos.payment.base.BaseSwipeCardActivity;
import com.nld.starpos.banktrade.activity.StartTransActivity;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.TransConstans;

/**
 * @author lin 2015年8月31日
 */
public class PreAuthSubmitActivity extends BaseSwipeCardActivity {

	@Override
	public void initViewData() {
		setTopTitle("预授权");
        Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_PRE);
		showTransMoney(false);
		showInputCarno(false);
	}

	@Override
	public Intent getNextStep() {
		Intent it = null;
    	if(Cache.getInstance().getSerInputCode().equals(TransConstans.INPUT_TYPE_QPBOC_NO_PIN)){
    	   //	it=new Intent(this, TransStartActivity.class);
          //   it.putExtra(TransStartActivity.TRANS_NEXT_ACTIVITY_TAG, PrintResultActivity.class.getName());
			it = new Intent(this, StartTransActivity.class);
    	} else{
    		it= new Intent(PreAuthSubmitActivity.this, PreAuthGetpinActivity.class);
    	}
		return it;
	}

}
