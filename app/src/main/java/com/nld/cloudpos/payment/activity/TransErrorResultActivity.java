package com.nld.cloudpos.payment.activity;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.activity.LauncherActivity;
import com.nld.cloudpos.payment.NldPaymentActivityManager;
import com.nld.cloudpos.payment.controller.AbstractActivity;
import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.ParamsConts;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.banktrade.utils.ShareBankPreferenceUtils;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.wxtrade.utils.ShareScanPreferenceUtils;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;

import common.DateTimeUtil;

public class TransErrorResultActivity extends AbstractActivity {

    private TextView mTransTv,mFailTip;
    private Button mConfirm;
    
    @Override
    public int contentViewSourceID() {
        return R.layout.act_trans_faild;
    }

    @Override
    public void initView() {
        mTransTv=(TextView) findViewById(R.id.faild_result_trans);
        mFailTip=(TextView) findViewById(R.id.faild_result_tip);
        mConfirm=(Button) findViewById(R.id.faild_result_confirm);
        setTopReturnListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finishErrorActivity();
            }
        });
        mConfirm.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                LogUtils.d("交易失败，发送广播");
                finishErrorActivity();
                
            }
        });
    }

    public void finishErrorActivity() {
      //  boolean isFirst = SharePreferenceUtils.getBoolean(TransParamsValue.PARAMS_KEY_IS_FIRST, true);
      //  String date = ParamsUtil.getInstance().getParam(ParamsConts.PARAMS_RUN_LOGIN_DATE);
      //  SharePreferenceUtils.putBoolean(ParamsConts.PARAMS_SIGN_SUCESS,false);
        ShareScanPreferenceUtils.putBoolean(this, TransParamsValue.PARAMS_KEY_IS_FIRST, false);
        ShareBankPreferenceUtils.putBoolean(ParamsConts.PARAMS_CARD_SETTLE_SUCESS,false);
        String transCode = Cache.getInstance().getTransCode();
        if (TransConstans.TRANS_CODE_SIGN.equals(transCode)){
            ParamsUtil.getInstance().update(ParamsConts.PARAMS_RUN_LOGIN_DATE, DateTimeUtil.getCurrentDate());
        }
       /* if (isFirst || !DateTimeUtil.getCurrentDate().equals(date)){
            Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_BATCHNO);
            startActivity(new Intent(TransErrorResultActivity.this,Network.class));
        }*/
        NldPaymentActivityManager.getActivityManager().removeAllActivityExceptOne(LauncherActivity.class);
    }

    @Override
    public void initData() {
        mTransTv.setText("交易失败");
        mFailTip.setText("错误码："+ Cache.getInstance().getErrCode()+","+ Cache.getInstance().getErrDesc());
    }

    @Override
    public void onServiceConnecteSuccess(AidlDeviceService service) {}

    @Override
    public void onServiceBindFaild() {}

    @Override
    public boolean saveValue() {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode== KeyEvent.KEYCODE_HOME ||
                keyCode== KeyEvent.KEYCODE_BACK){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
