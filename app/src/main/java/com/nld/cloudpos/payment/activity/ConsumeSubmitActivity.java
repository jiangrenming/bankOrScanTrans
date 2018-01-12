package com.nld.cloudpos.payment.activity;

import android.content.Intent;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.base.BaseSwipeCardActivity;
import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.activity.StartTransActivity;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.TransConstans;

/**
 * 刷卡界面
 * 
 * @author Tianxiaobo
 * 
 */
public class ConsumeSubmitActivity extends BaseSwipeCardActivity {
    @Override
    public void initViewData() {
        setTopTitle("消费");

        showTransMoney(true);
        showInputCarno(false);
        iv_bottom.setImageResource(R.drawable.pic_consume_tips);
    }

    @Override
    public Intent getNextStep() {
        Intent it = null;
        //qpboc 联机低于三百不需要密码界面 直接交易
     //   it = new Intent(mContext,ConsumeGetpinActivity.class);
        if(Cache.getInstance().getSerInputCode().equals(TransConstans.INPUT_TYPE_QPBOC_NO_PIN)){
            LogUtils.i("无须输入密码，直接调起银行支付");
            it=new Intent(this, StartTransActivity.class);
         //   it.putExtra(TransStartActivity.TRANS_NEXT_ACTIVITY_TAG, PrintResultActivity.class.getName());
        } else{
            it = new Intent(mContext,ConsumeGetpinActivity.class);
        }
        return it;
    }
}
