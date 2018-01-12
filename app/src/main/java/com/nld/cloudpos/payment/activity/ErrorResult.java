package com.nld.cloudpos.payment.activity;

import android.content.Intent;
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
import com.nld.starpos.banktrade.bean.ResultStatus;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.Constant;
import com.nld.starpos.banktrade.utils.TransConstans;

import common.StringUtil;

public class ErrorResult extends AbstractActivity {

	private TextView mTransTv, mFailTip;
	private Button mConfirm;

	@Override
	public int contentViewSourceID() {
		return R.layout.act_trans_faild;
	}

	@Override
	public void initView() {
	    setTopReturnListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMainmenu();
            }
        });
		mTransTv = (TextView) findViewById(R.id.faild_result_trans);
		mFailTip = (TextView) findViewById(R.id.faild_result_tip);
		mConfirm = (Button) findViewById(R.id.faild_result_confirm);
		mConfirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
	            backToMainmenu();
			}
		});
	}

	@Override
	public void initData() {
		ResultStatus result = Cache.getInstance().getRestatus();
		String retCode = "";
		String errMsg = "";
		if(result!=null){
			retCode = result.getRetCode();
			errMsg = result.getErrMsg();
		}else{
			retCode = Cache.getInstance().getErrCode();
			errMsg = Cache.getInstance().getErrDesc();
		}

		mTransTv.setText("交易失败");
		mFailTip.setText("错误码：" + retCode + "\n" + errMsg);
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
		ResultStatus restatus = Cache.getInstance().getRestatus();
		if (null != restatus){
			String transCode = restatus.getTransCode();
			if (!StringUtil.isEmpty(transCode)){
				if (TransConstans.TRANS_CODE_PARAMS.equals(transCode)){ //同步业务参数失败
					Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_MERNOINFO);
					startActivity(new Intent(this, Network.class));
				}else if (TransConstans.TRANS_CODE_MERNOINFO.equals(transCode)){ //同步商终信息失败 <同步扫码批次号>
					Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_BATCHNO);
					startActivity(new Intent(this, Network.class));
				}else if (TransConstans.TRANS_CODE_BATCHNO.equals(transCode)){ //同步批次号失败 <签到>
					Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_SIGN);
					//startActivity(new Intent(this, Network.class));
					startActivityForResult(new Intent(this, Network.class), Constant.BANK_SIGN );
				}
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK){
			if (requestCode == Constant.BANK_SIGN  && null != data){
				String transResultTip = data.getStringExtra("transResultTip");
				Intent intent = new Intent(ErrorResult.this, SignSuccessActivity.class);
				intent.putExtra("transResultTip",transResultTip);
				startActivity(intent);
			}
		}else if (resultCode == RESULT_FIRST_USER){
			startActivity(new Intent(ErrorResult.this, TransErrorResultActivity.class));
		}
	}
}