package com.nld.cloudpos.payment.activity;

import android.content.Intent;
import com.nld.cloudpos.payment.base.BaseGetPinActivity;
import com.nld.starpos.banktrade.activity.StartTransActivity;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.TransConstans;

/**
 * 输入PIN界面
 * @author Tianxiaobo
 *
 */
public class ConsumeGetpinActivity extends BaseGetPinActivity {

    @Override
    public void initViewData() {
        String transCode = Cache.getInstance().getTransCode();
        if (TransConstans.TRANS_CODE_CONSUME.equals(transCode)){
            setTopTitle("消费");
        }else if (TransConstans.TRANS_CODE_PRE_COMPLET.equals(transCode)){
            setTopTitle("预授权完成请求");
        }else if (TransConstans.TRANS_CODE_PRE_CX.equals(transCode)){
            setTopTitle("预授权撤销");
        }
    }

    @Override
    public Intent getNextStep() {
        Intent it = new Intent(this, StartTransActivity.class);
        //Intent it=new Intent(this, TransStartActivity.class);
       // it.putExtra(TransStartActivity.TRANS_NEXT_ACTIVITY_TAG, PrintResultActivity.class.getName());
        return it;
    }

}
