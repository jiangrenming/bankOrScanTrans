package com.nld.cloudpos.ecash.activity;

import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.base.BaseAbstractActivity;
import com.nld.cloudpos.util.MyLog;

public class PbocWholeProcess extends BaseAbstractActivity {
	
	private final MyLog logger = MyLog.getLogger(this.getClass());

	private String transCode;
	
	@Override
	public int contentViewSourceID() {
		 return R.layout.act_trans_start;
	}

	@Override
	public void initView() {

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

}
