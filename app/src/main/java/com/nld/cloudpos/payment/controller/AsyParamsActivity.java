package com.nld.cloudpos.payment.controller;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.NldPaymentActivityManager;
import com.nld.cloudpos.payment.activity.Network;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.wxtrade.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.utils.ShareScanPreferenceUtils;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;

/**
 * Created by jiangrenming on 2017/10/18.
 * 同步业务参数
 */

public class AsyParamsActivity  extends AbstractActivity implements View.OnClickListener{

    @ViewInject(R.id.update_param_scanphone_alipay_img)
    ImageView mAlipayPhoneImg;
    @ViewInject(R.id.update_param_scanpos_alipay_img)
    ImageView mAlipayPosImg;
    @ViewInject(R.id.update_param_scanphone_weixin_img)
    ImageView mWeiXinPhoneImg;
    @ViewInject(R.id.update_param_scanpos_weixin_img)
    ImageView mWeiXinPosImg;
    @ViewInject(R.id.update_param_scanrefund_img)
    ImageView mScanRefundImg;
    @ViewInject(R.id.update_param_storeName)
    TextView update_param_storeName;
    @ViewInject(R.id.tv_update_confirm)
    TextView tv_update_confirm;

    @Override
    public int contentViewSourceID() {
        return R.layout.update_params_fragment;
    }

    @Override
    public void initView() {
        ViewUtils.inject(this);
        tv_update_confirm.setOnClickListener(this);
    }

    @Override
    public void initData() {
        setTopTitle("业务开通状态");
        String storeShortName = getText(R.string.store_short_name) + ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_MERCHANTNAME);
        update_param_storeName.setText(storeShortName);
        setOpen(mAlipayPhoneImg, ShareScanPreferenceUtils.getBoolean(AsyParamsActivity.this, TransParamsValue.PARAMS_KEY_TRANS_SCAN_PHONE_ALIPAY,false));
        setOpen(mAlipayPosImg,  ShareScanPreferenceUtils.getBoolean(AsyParamsActivity.this, TransParamsValue.PARAMS_KEY_TRANS_SCAN_POS_ALIPAY,false));
        setOpen(mWeiXinPhoneImg,  ShareScanPreferenceUtils.getBoolean(AsyParamsActivity.this, TransParamsValue.PARAMS_KEY_TRANS_SCAN_PHONE_WEIXIN,false));
        setOpen(mWeiXinPosImg,  ShareScanPreferenceUtils.getBoolean(AsyParamsActivity.this, TransParamsValue.PARAMS_KEY_TRANS_SCAN_POS_WEIXIN,false));
        setOpen(mScanRefundImg,  ShareScanPreferenceUtils.getBoolean(AsyParamsActivity.this, TransParamsValue.PARAMS_KEY_TRANS_SCAN_REFUND,false));
        setTopReturnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });
    }

    private void back() {

        boolean flag = ShareScanPreferenceUtils.getBoolean(this, TransParamsValue.PARAMS_IS_PARAM, false);
        if (flag){
            Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_MERNOINFO);
            startActivity(new Intent(this, Network.class));
        }
        NldPaymentActivityManager.getActivityManager()
                .removeActivity(AsyParamsActivity.this);
    }

    private void setOpen(ImageView imgView, boolean flag) {
        if (flag) {
            imgView.setImageResource(R.drawable.yewukaitong_icon_chenggong);
        } else {
            imgView.setImageResource(R.drawable.yewukaitong_icon_shibai);
        }
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
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_update_confirm:
                back();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME ||
                keyCode == KeyEvent.KEYCODE_BACK) {
            back();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
