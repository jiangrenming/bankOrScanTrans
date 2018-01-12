package com.nld.cloudpos.payment.activity;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.controller.AbstractActivity;
import com.nld.cloudpos.payment.controller.FormatUtils;
import com.nld.starpos.banktrade.utils.ParamsConts;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.wxtrade.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.utils.ShareScanPreferenceUtils;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;

public class SignSuccessActivity extends AbstractActivity {
    private TextView mTip;
    private TextView mResultTitle;
    private Button mConfirm;
    private TextView tv_content;

    @Override
    public int contentViewSourceID() {
        return R.layout.act_sign_success;
    }

    @Override
    public void initView() {
        setTopReturnListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finishTrans();
            }
        });
        mConfirm = (Button) findViewById(R.id.sign_success_confirm);
        mTip = (TextView) findViewById(R.id.sign_success__msg);
        mTip.setVisibility(View.GONE);
        mResultTitle = (TextView) findViewById(R.id.sign_title);
        tv_content = (TextView) findViewById(R.id.tv_content);
        mConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finishTrans();
            }
        });
    }

    @Override
    public void initData() {
        String tip = getIntent().getStringExtra("transResultTip");
        setTopTitle(tip);
        mResultTitle.setText(tip);
        String bankMerNa = ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID);
        //String bankCardNu = ParamsUtil.getInstance(this).getParam(ParamsConts.BindParamsContns.PARAMS_KEY_CARD_ACCOUNT);
        String scanMerNa = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID);
        String scanaCount = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_QR_CODE_ACCOUNT);
        String content = "";
        if (!TextUtils.isEmpty(bankMerNa)) {
            content = "业务产品:银行卡收款\n商户编号:" + bankMerNa+ "\n\n";
    /*                + "\n结算账户:" + FormatUtils.formatCardNoWithStar(bankCardNu) + "\n\n";*/
        }
        if (!TextUtils.isEmpty(scanMerNa)) {
            content += "业务产品:二维码收款\n商户编号:" + scanMerNa
                    + "\n结算账户:" + FormatUtils.formatCardNoWithStar(scanaCount);
        }
        if (!content.equals("")) {
            tv_content.setText(content);
        } else {
            tv_content.setText("");
        }
    }

    public void finishTrans() {
      //  boolean isFirst = SharePreferenceUtils.getBoolean(TransParamsValue.PARAMS_KEY_IS_FIRST, true);
     //   String date = ParamsUtil.getInstance(this).getParam(ParamsConts.PARAMS_RUN_LOGIN_DATE);
       // SharePreferenceUtils.putBoolean(ParamsConts.PARAMS_SIGN_SUCESS,false);
        ShareScanPreferenceUtils.putBoolean(SignSuccessActivity.this, TransParamsValue.PARAMS_KEY_IS_FIRST, false);
      /*  if (isFirst || !DateTimeUtil.getCurrentDate().equals(date)){
            Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_BATCHNO);
            startActivity(new Intent(SignSuccessActivity.this,Network.class));
        }*/
        finish();
    }

    @Override
    public void onServiceConnecteSuccess(AidlDeviceService service) {
    }

    @Override
    public void onServiceBindFaild() {
    }

    @Override
    public boolean saveValue() {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME ||
                keyCode == KeyEvent.KEYCODE_BACK) {
            finishTrans();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}