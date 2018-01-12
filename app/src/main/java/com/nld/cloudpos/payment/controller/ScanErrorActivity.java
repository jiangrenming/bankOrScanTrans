package com.nld.cloudpos.payment.controller;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.activity.LauncherActivity;
import com.nld.cloudpos.payment.NldPaymentActivityManager;
import com.nld.cloudpos.payment.activity.Network;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.wxtrade.bean.scan_common.ResultStatus;
import com.nld.starpos.wxtrade.bean.scan_common.ScanCache;
import com.nld.starpos.wxtrade.utils.params.ScanTransFlagUtil;

import common.StringUtil;

/**
 * Created by jiangrenming on 2017/11/1.
 * 扫码错误界面
 */

public class ScanErrorActivity extends AbstractActivity {

    private TextView mTransTv, mFailTip;
    private Button mConfirm;

    @Override
    public int contentViewSourceID() {
        return R.layout.act_trans_faild;
    }

    @Override
    public void initView() {
        setTopReturnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMainmenu();
            }
        });
        mTransTv = (TextView) findViewById(R.id.faild_result_trans);
        mFailTip = (TextView) findViewById(R.id.faild_result_tip);
        mConfirm = (Button) findViewById(R.id.faild_result_confirm);
        mConfirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                backToMainmenu();
            }
        });
    }

    @Override
    public void initData() {
        ResultStatus resultStatus = ScanCache.getInstance().getResultStatus();
        String retCode = "";
        String errMsg = "";
        if(resultStatus !=null){
            retCode = resultStatus.getRetCode();
            errMsg = resultStatus.getErrMsg();
        }else{
            retCode = ScanCache.getInstance().getErrCode();
            errMsg = ScanCache.getInstance().getErrMesage();
        }

        mTransTv.setText(R.string.str_deal_fail);
        mFailTip.setText(getString(R.string.str_error_code) + retCode + "\n" + errMsg);
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
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode== KeyEvent.KEYCODE_BACK
                || keyCode== KeyEvent.KEYCODE_HOME
                || keyCode== KeyEvent.KEYCODE_MENU){
            backToMainmenu();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
    private void backToMainmenu(){
        NldPaymentActivityManager.getActivityManager()
                .removeAllActivityExceptOne(LauncherActivity.class);
       ResultStatus restatus = ScanCache.getInstance().getResultStatus();
        if (null != restatus){
            String transCode = restatus.getTransCode();
            if (!StringUtil.isEmpty(transCode)){
                if (ScanTransFlagUtil.TRANS_CODE_PARAMS.equals(transCode)) { //同步业务参数失败
                    Cache.getInstance().setTransCode(ScanTransFlagUtil.TRANS_CODE_MERNOINFO);
                    startActivity(new Intent(this, Network.class));
                }
            }
        }
    }
}
