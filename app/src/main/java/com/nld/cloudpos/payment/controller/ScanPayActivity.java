package com.nld.cloudpos.payment.controller;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.view.CalculatorView;
import com.nld.cloudpos.payment.activity.ErrorResult;
import com.nld.cloudpos.payment.activity.PrintResultActivity;
import com.nld.cloudpos.util.CommonContants;
import com.nld.starpos.wxtrade.activity.StartScanActivity;
import com.nld.starpos.wxtrade.bean.scan_common.ScanCache;
import com.nld.starpos.wxtrade.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.local.db.bean.ScanTransRecord;
import com.nld.starpos.wxtrade.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.utils.ShareScanPreferenceUtils;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import com.nld.starpos.wxtrade.utils.params.CommonParams;
import com.nld.starpos.wxtrade.utils.params.EncodingEmun;
import com.nld.starpos.wxtrade.utils.params.ScanTransFlagUtil;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;
import com.nld.starpos.wxtrade.utils.params.TransType;

import common.StringUtil;

/**
 * Created by jiangrenming on 2017/9/28.
 * 二维码消费界面
 */

public class ScanPayActivity extends AbstractActivity implements View.OnClickListener{

    private AidlDeviceService mService;
    @ViewInject(R.id.cv_calculator)
    CalculatorView calculatorView;
    @ViewInject(R.id.rl_weChat)
    RelativeLayout weChat;
    @ViewInject(R.id.rl_aliPay)
    RelativeLayout aliPay;

    private String mPayMoney; // 最后确认的钱数
    private String artStr;
    private int scan_pos_phone;

    @Override
    public int contentViewSourceID() {
        return R.layout.scan_pay_layout;
    }

    @Override
    public void initView() {
        ViewUtils.inject(this);
        weChat.setOnClickListener(this);
        aliPay.setOnClickListener(this);
        setTopDefaultReturn();
    }

    @Override
    public void initData() {
        setTopTitle("二维码收款");
    }

    @Override
    public void onServiceConnecteSuccess(AidlDeviceService service) {
        this.mService = service;
    }

    @Override
    public void onServiceBindFaild() {}

    @Override
    public boolean saveValue() {
        return false;
    }

    @Override
    public void onClick(View view) {
        if (!checkValue()){
            return;
        }
        scan_pos_phone = ShareScanPreferenceUtils.getInt(this, TransParamsValue.PARAMS_KEY_SCAN_POS_PHONE, 0);
        ScanPayBean scanBean = null;
        switch (view.getId()){
            case  R.id.rl_weChat:
                 scanBean = getScanPay(TransType.MainTransType.WECHAT,scan_pos_phone);
                if (scan_pos_phone == 0){  //扫手机<这里还会分是前置还是后置扫码>
                    if (!ShareScanPreferenceUtils.getBoolean(this, TransParamsValue.PARAMS_KEY_TRANS_SCAN_PHONE_WEIXIN,false)) {
                        ToastUtils.showToast("该功能尚未开通，请联系管理员");
                        return;
                    }
                    if (!TransUtils.isExitLocalScanData()){
                        ToastUtils.showToast("无法获取到商终,请重启试试，谢谢");
                        return;
                    }
                    new CommonScanPay(scan_pos_phone,this,scanBean,scanPayHandle).scan_pay();
                }else {   //商户手机扫Pos二维码<快速支付>
                    if (!ShareScanPreferenceUtils.getBoolean(this, TransParamsValue.PARAMS_KEY_TRANS_SCAN_PHONE_WEIXIN,false)) {
                        ToastUtils.showToast("该功能尚未开通，请联系管理员");
                        return;
                    }
                    if (!TransUtils.isExitLocalScanData()){
                        ToastUtils.showToast("无法获取到商终，请重启试试，谢谢");
                        return;
                    }
                    gotoNextStartActivity(scanBean,scan_pos_phone);
                }
            break;
            case R.id.rl_aliPay:
                 scanBean = getScanPay(TransType.MainTransType.ALIPAY,scan_pos_phone);
                if (scan_pos_phone == 0){  //扫手机
                    if (!ShareScanPreferenceUtils.getBoolean(this, TransParamsValue.PARAMS_KEY_TRANS_SCAN_PHONE_WEIXIN,false)) {
                        ToastUtils.showToast("该功能尚未开通，请联系管理员");
                        return;
                    }
                    if (!TransUtils.isExitLocalScanData()){
                        ToastUtils.showToast("无法获取到商终，请重启试试，谢谢");
                        return;
                    }
                    new CommonScanPay(scan_pos_phone,this,scanBean,scanPayHandle).scan_pay();
                }else {   //手机扫POS二维码
                    if (!ShareScanPreferenceUtils.getBoolean(this, TransParamsValue.PARAMS_KEY_TRANS_SCAN_PHONE_WEIXIN,false)) {
                        ToastUtils.showToast("该功能尚未开通，请联系管理员");
                        return;
                    }
                    if (!TransUtils.isExitLocalScanData()){
                        ToastUtils.showToast("无法获取到商终，请重启试试，谢谢");
                        return;
                    }
                    gotoNextStartActivity(scanBean,scan_pos_phone);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 区分微信，支付宝扫手机支付
     * @param type
     * @return
     */
    private ScanPayBean getScanPay(int type, int qc_ScanCode){
        ScanPayBean scanBean = new ScanPayBean();
        scanBean.setAmount(Long.valueOf(artStr)); //支付金额
        scanBean.setCurrency(EncodingEmun.CNYCURRENCY.getType());
        scanBean.setFormat(2);
        scanBean.setMerchantName(ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_MERCHANTNAME)); //商户名称
        scanBean.setTerminalNo(ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID));  //终端号
        scanBean.setRequestUrl(CommonContants.url);
        scanBean.setProjectType(EncodingEmun.antCompany.getType());
        String posSn = ShareScanPreferenceUtils.getString(this, CommonParams.POSSn, null);
        if (!StringUtil.isEmpty(posSn)) {
            scanBean.setSn(ShareScanPreferenceUtils.getString(ScanPayActivity.this, CommonParams.POSSn,null));  //终端序列号
        }
        scanBean.setTxnCnl(TransType.ScanTransType.TRANS_SCAN_PAY_I);  //交易渠道
        scanBean.setBatchNo(ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_TYANS_BATCHNO)); //批次号
        scanBean.setTransNo(ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_SYSTRANCE_NO));  //流水号
        switch (type){
            case TransType.MainTransType.WECHAT:
                if (qc_ScanCode == 0){
                    scanBean.setTransType(TransParamsValue.AntCompanyInterfaceType.WXPOS); //接口类型
                    scanBean.setType(TransType.ScanTransType.TRANS_SCAN_WEIXIN);  //微信扫手机支付
                }else {
                    scanBean.setTransType(TransParamsValue.AntCompanyInterfaceType.SCAN_QC_POS); //接口类型
                    scanBean.setType(TransType.ScanTransType.TRANS_QR_WEIXIN);  //客户扫pos支付类型
                }
                scanBean.setPayChannel(TransType.ScanTransType.TRANS_SCAN_WX_CHANNCLE); //支付渠道
                break;
            case TransType.MainTransType.ALIPAY:
                if (qc_ScanCode == 0){
                    scanBean.setTransType(TransParamsValue.AntCompanyInterfaceType.WXPOS); //接口类型
                    scanBean.setType(TransType.ScanTransType.TRANS_SCAN_ALIPAY);  //支付宝扫手机交易类型
                }else {
                    scanBean.setTransType(TransParamsValue.AntCompanyInterfaceType.SCAN_QC_POS); //接口类型
                    scanBean.setType(TransType.ScanTransType.TRANS_QR_ALIPAY);  //客户扫pos支付类型
                }
                scanBean.setPayChannel(TransType.ScanTransType.TRANS_SCAN_ALIPAY_CHANNCLE); //支付宝支付渠道
                break;
            default:
                break;
        }
        return scanBean;
    }
    /**
     * 检测输入值是否过关
     * @return
     */
    private boolean checkValue() {
        mPayMoney = calculatorView.getPayMoney();
        if (mPayMoney == null || TextUtils.equals(mPayMoney, "0.00")) {
            ToastUtils.showToast(getString(R.string.cash_pay_input_money));
            return false;
        }
        artStr= mPayMoney.replace(".", "").trim();
        if (Long.valueOf(artStr) < 0) {
            ToastUtils.showToast(R.string.cash_pay_more_than_0);
            return false;
        }
        if (Long.valueOf(artStr) / 100  > TransType.MainTransType.PAY_MAX_VALUE) {
            ToastUtils.showToast(getString(R.string.cash_more_than_max));
            return false;
        }
        return true;
    }

    private Handler scanPayHandle = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case  0x11 :
                    ScanPayBean scan = (ScanPayBean) msg.getData().getSerializable("scan");
                    if (scan != null){
                        gotoNextStartActivity(scan,scan_pos_phone);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private  void  gotoNextStartActivity(ScanPayBean scan, int scan_pos_phone){
        ScanCache.getInstance().setTransCode(ScanTransFlagUtil.TRANS_CODE_WX_PAY);
        Intent intent = new Intent(ScanPayActivity.this, StartScanActivity.class);
        intent.putExtra("scan",scan);
        switch (scan_pos_phone){
            case 0:
                startActivityForResult(intent, TransType.SCAN_PAY_CALLBACK_CODE);
                break;
            case 1:
                startActivityForResult(intent, TransType.SCAN_POS_CODE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == TransType.SCAN_PAY_CALLBACK_CODE){
                if (data != null){
                    ScanTransRecord water = (ScanTransRecord)data.getSerializableExtra("water");
                    if (water != null){
                        Intent printerIntent = new Intent(ScanPayActivity.this,PrintResultActivity.class);
                        printerIntent.putExtra("water", water);
                        printerIntent.putExtra("transType", String.valueOf(water.getTransType()));
                        startActivity(printerIntent);
                    }
                }
            }else if (requestCode == TransType.SCAN_POS_CODE){ //扫pos
                ScanPayBean scanPayBean = (ScanPayBean) data.getSerializableExtra("scan_pos");
                if (null != scanPayBean) {  //跳转到生成二维码的界面
                    Intent intent = new Intent(ScanPayActivity.this, QCImageActivity.class);
                    intent.putExtra("scan_pos", scanPayBean);
                    startActivity(intent);
                }
            }
        }else if (resultCode == RESULT_FIRST_USER){
            Intent printerIntent = new Intent(ScanPayActivity.this,ErrorResult.class);
            startActivity(printerIntent);
        }else if (resultCode == RESULT_CANCELED){
            ToastUtils.showToast("交易取消");
        }
    }
}
