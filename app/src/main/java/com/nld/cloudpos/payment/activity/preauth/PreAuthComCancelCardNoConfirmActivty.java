/**
 * 
 */
package com.nld.cloudpos.payment.activity.preauth;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.activity.PrintResultActivity;
import com.nld.cloudpos.payment.activity.TransStartActivity;
import com.nld.cloudpos.payment.base.BaseAbstractActivity;
import com.nld.starpos.banktrade.utils.Cache;

/**
 * 预授权完成撤销
 * @author lin
 * 2015年8月31日
 */
public class PreAuthComCancelCardNoConfirmActivty extends BaseAbstractActivity{

	private TextView mCardno = null; //
	private TextView tv_authcode = null; // 授权码
	private TextView tv_name;
	private TextView tv_money;
	private TextView mType=null;//消费类型
	private Button mConfirm;
	

	private String paymoneyStr = null;
	private String cardnoStr = null;
	
	@Override
	public int contentViewSourceID() {
		return R.layout.preauth_complete_cardinfo_confirm;
	}

	@Override
	public void initView() {
		 setTopDefaultReturn();
	        setTopTitle("核对交易信息");
	        mCardno = (TextView) super.findViewById(R.id.confirm_carno);
	        tv_authcode = (TextView) super.findViewById(R.id.tv_authcode);
	        tv_money = (TextView) super.findViewById(R.id.tv_money);
	        tv_name = (TextView) super.findViewById(R.id.tv_name);
	        mType=(TextView) findViewById(R.id.confirm_type);
	        mConfirm=(Button) findViewById(R.id.confirm_btn);
	        mCardno.setText(Cache.getInstance().getCardNo());
	        tv_authcode.setText(Cache.getInstance().getSerialNo());
	        tv_money.setText(Cache.getInstance().getTransMoney());
	        mType.setText("预授权完成撤销");
	        tv_name.setText("原交易凭证号");
	        mConfirm.setOnClickListener(new OnClickListener() {
	            
	            @Override
	            public void onClick(View v) {
	                Intent it=new Intent(mContext,TransStartActivity.class);
	                it.putExtra(TransStartActivity.TRANS_NEXT_ACTIVITY_TAG, PrintResultActivity.class.getName());
	                goToNextActivity(it);
	            }
	        });
	        
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
