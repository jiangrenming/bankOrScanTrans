package com.nld.cloudpos.bankline.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.view.CalculatorView;
import com.nld.cloudpos.payment.activity.ConsumeSubmitActivity;
import com.nld.cloudpos.payment.activity.Network;
import com.nld.cloudpos.payment.activity.PrintResultActivity;
import com.nld.cloudpos.payment.activity.SignSuccessActivity;
import com.nld.cloudpos.payment.activity.TransErrorResultActivity;
import com.nld.cloudpos.payment.controller.CommonScanPay;
import com.nld.cloudpos.payment.controller.QCImageActivity;
import com.nld.cloudpos.payment.controller.ScanErrorActivity;
import com.nld.cloudpos.payment.controller.TransUtils;
import com.nld.cloudpos.util.CommonContants;
import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.activity.StartTransActivity;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.Constant;
import com.nld.starpos.banktrade.utils.ParamsConts;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.banktrade.utils.ShareBankPreferenceUtils;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.banktrade.utils.TransParams;
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
import java.text.DecimalFormat;
import common.DateTimeUtil;
import common.StringUtil;
import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_FIRST_USER;
import static android.app.Activity.RESULT_OK;

/**
 * Created by L on 2017/2/7.
 * @描述 收银页面。
 */

public class CashFragment extends BaseFragment implements View.OnClickListener {

    private String mPayMoney; // 最后确认的钱数
    private RelativeLayout mRlBrush;
    private RelativeLayout mRlWeChat;
    private RelativeLayout mRlAliPay;
    private CalculatorView mCvCalculator;
    private String artStr;
    private int scan_pos_phone;

    @Override
    public int doGetContentViewId() {
        return R.layout.fragment_cash;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {
        mRlBrush = queryViewById(R.id.rl_brush);
        mRlWeChat = queryViewById(R.id.rl_weChat);
        mRlAliPay = queryViewById(R.id.rl_aliPay);
        mCvCalculator = queryViewById(R.id.cv_calculator);
        mRlBrush.setOnClickListener(this);         // 刷卡
        mRlWeChat.setOnClickListener(this);        // 微信
        mRlAliPay.setOnClickListener(this);        // 支付宝
    }

    @Override
    public void onClick(View view) {
        mPayMoney = mCvCalculator.getPayMoney();
        switch (view.getId()) {
            case R.id.rl_brush: // 刷卡
                toPay(TransType.MainTransType.BRUSH);
                break;
            case R.id.rl_weChat: // 微信
                toPay(TransType.MainTransType.WECHAT);
                break;
            case R.id.rl_aliPay: // 支付宝
                toPay(TransType.MainTransType.ALIPAY);
                break;
            default:
                break;
        }
    }

    /**
     * 三种支付方式。
     *
     * @param type
     */
    private void toPay(int type) {
        if (mPayMoney == null || TextUtils.equals(mPayMoney, "0.00")) {
            ToastUtils.showToast(getString(R.string.cash_pay_input_money));
            return;
        }
        artStr= mPayMoney.replace(".", "").trim();
        if (Long.valueOf(artStr) < 0) {
            ToastUtils.showToast(R.string.cash_pay_more_than_0);
            return;
        }
        if (Long.valueOf(artStr) / 100  > TransType.MainTransType.PAY_MAX_VALUE) {
            ToastUtils.showToast(getString(R.string.cash_more_than_max));
            return;
        }
        goToNextStep(type);
    }

    /**
     * 跳转到对应结算页面。
     *
     * @param payType
     */
    private void goToNextStep(int payType) {
         scan_pos_phone = ShareScanPreferenceUtils.getInt(getActivity(), TransParamsValue.PARAMS_KEY_SCAN_POS_PHONE, 0);
        if ( payType == TransType.MainTransType.WECHAT ) {  //微信扫码支付
            ScanPayBean scanBean = getScanPay(payType,scan_pos_phone);
                if (scan_pos_phone == 0){  //扫手机<这里还会分是前置还是后置扫码>
                    if (!ShareScanPreferenceUtils.getBoolean(getActivity(), TransParamsValue.PARAMS_KEY_TRANS_SCAN_PHONE_WEIXIN,false)) {
                        ToastUtils.showToast("该功能尚未开通，请联系管理员");
                        return;
                    }
                    if (!isExitDatas()){
                        ToastUtils.showToast("无法获取到商终，请重启试试，谢谢");
                        return;
                    }
                    new CommonScanPay(scan_pos_phone,getActivity(),scanBean,scanPayHandle).scan_pay();
                }else {   //商户手机扫Pos二维码<快速支付>
                    if (!ShareScanPreferenceUtils.getBoolean(getActivity(), TransParamsValue.PARAMS_KEY_TRANS_SCAN_POS_WEIXIN,false)) {
                        ToastUtils.showToast("该功能尚未开通，请联系管理员");
                        return;
                    }
                    if (!isExitDatas()){
                        ToastUtils.showToast("无法获取到商终，请重启试试，谢谢");
                        return;
                    }
                    gotoNextStartActivity(scanBean,scan_pos_phone);
                }
        }else if (payType  == TransType.MainTransType.ALIPAY){  //支付宝扫码支付
            ScanPayBean scanBean = getScanPay(payType,scan_pos_phone);
                if (scan_pos_phone == 0){  //扫手机
                    if (!ShareScanPreferenceUtils.getBoolean(getActivity(), TransParamsValue.PARAMS_KEY_TRANS_SCAN_PHONE_ALIPAY,false)) {
                        ToastUtils.showToast("该功能尚未开通，请联系管理员");
                        return;
                    }
                    if (!isExitDatas()){
                        ToastUtils.showToast("无法获取到商终，请重启试试，谢谢");
                        return;
                    }
                    new CommonScanPay(scan_pos_phone,getActivity(),scanBean,scanPayHandle).scan_pay();
                }else {   //手机扫POS二维码
                    if (!ShareScanPreferenceUtils.getBoolean(getActivity(), TransParamsValue.PARAMS_KEY_TRANS_SCAN_POS_ALIPAY,false)) {
                        ToastUtils.showToast("该功能尚未开通，请联系管理员");
                        return;
                    }
                    if (!isExitDatas()){
                        ToastUtils.showToast("无法获取到商终，请重启试试，谢谢");
                        return;
                    }
                    gotoNextStartActivity(scanBean,scan_pos_phone);
                }
        } else if (payType == TransType.MainTransType.BRUSH){
            DecimalFormat decfmat = new DecimalFormat("#######0.00");
            Cache.getInstance().setTransMoney(decfmat.format(Double.valueOf(mPayMoney)));
            Intent intent = new Intent();
            Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_CONSUME);
            intent.setClass(mActivity, ConsumeSubmitActivity.class);
            startActivity(intent);
        }
    }

    private boolean isExitDatas(){
        if (!TransUtils.isExitLocalScanData()){
            return  false;
        }
        return true;
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
        scanBean.setRequestUrl(CommonContants.url);  //项目路径
        scanBean.setProjectType(EncodingEmun.antCompany.getType()); //项目类型
        String posSn = ShareScanPreferenceUtils.getString(getActivity(), CommonParams.POSSn, null);
        if (!StringUtil.isEmpty(posSn)) {
            scanBean.setSn(ShareScanPreferenceUtils.getString(getActivity(), CommonParams.POSSn,null));  //终端序列号
        }
        scanBean.setTxnCnl(TransType.ScanTransType.TRANS_SCAN_PAY_I);  //交易渠道
        scanBean.setBatchNo(ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_TYANS_BATCHNO)); //批次号
        LogUtils.i("新的批次号="+ ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_TYANS_BATCHNO));
        scanBean.setTransNo(ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_SYSTRANCE_NO));  //流水号
        switch (type){
            case TransType.MainTransType.WECHAT:
                if (qc_ScanCode == 0){
                    scanBean.setTransType(TransParamsValue.InterfaceType.WXPOS); //接口类型
                    scanBean.setType(TransType.ScanTransType.TRANS_SCAN_WEIXIN);  //微信扫手机支付
                }else {
                    scanBean.setTransType(TransParamsValue.InterfaceType.SCAN_QC_POS); //接口类型
                    scanBean.setType(TransType.ScanTransType.TRANS_QR_WEIXIN);  //客户扫pos支付类型
                }
                scanBean.setPayChannel(TransType.ScanTransType.TRANS_SCAN_WX_CHANNCLE); //支付渠道
                break;
            case TransType.MainTransType.ALIPAY:
                if (qc_ScanCode == 0){
                    scanBean.setTransType(TransParamsValue.InterfaceType.WXPOS); //接口类型
                    scanBean.setType(TransType.ScanTransType.TRANS_SCAN_ALIPAY);  //支付宝扫手机交易类型
                }else {
                    scanBean.setTransType(TransParamsValue.InterfaceType.SCAN_QC_POS); //接口类型
                    scanBean.setType(TransType.ScanTransType.TRANS_QR_ALIPAY);  //客户扫pos支付类型
                }
                scanBean.setPayChannel(TransType.ScanTransType.TRANS_SCAN_ALIPAY_CHANNCLE); //支付宝支付渠道
                break;
            default:
                break;
        }
        return scanBean;
    }

    @Override
    public void onResume() {
        super.onResume();
        mCvCalculator.initMoney();

        boolean result = ShareScanPreferenceUtils.getBoolean(getActivity(), TransParamsValue.PARAMS_KEY_IS_FIRST, true);
        String date = ParamsUtil.getInstance().getParam(ParamsConts.PARAMS_RUN_LOGIN_DATE);
        String signTag = ParamsUtil.getInstance().getParam(ParamsConts.SIGN_SYMBOL);
     //   boolean signSucess = SharePreferenceUtils.getBoolean(ParamsConts.PARAMS_SIGN_SUCESS, true);
        LogUtils.i("签到的时间"+date+"/当前系统时间"+ DateTimeUtil.getCurrentDate()+"/是否是第一次启动="+result+"/签到的标志="+signTag);
        if (result /*&& signSucess*/){
            //业务参数请求
            asyParams();
        }else {
            if (!StringUtil.isEmpty(date)){
                if (!DateTimeUtil.getCurrentDate().equals(date)){
                    asyParams();
                }else {
                     boolean settle_sucess = ShareBankPreferenceUtils.getBoolean(ParamsConts.PARAMS_CARD_SETTLE_SUCESS, true);
                    if (TransParams.SingValue.UnSingedValue.equals(signTag) && settle_sucess){
                         Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_SIGN);
                       //  startActivity(new Intent(getActivity(), Network.class));
                        startActivityForResult(new Intent(getActivity(), StartTransActivity.class), Constant.BANK_SIGN);
                    }
                }
            }
        }
    }
    /**
     * 当首次安装应用或日切时调用业务请求参数
     */
    private void asyParams() {

        ShareScanPreferenceUtils.putBoolean(getActivity(), TransParamsValue.PARAMS_IS_PARAM,true);
        Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_PARAMS);

        ScanPayBean scanPayBean = new ScanPayBean();
        scanPayBean.setRequestUrl(CommonContants.url);
        scanPayBean.setTransType(TransParamsValue.InterfaceType.POSPARMSET);
        scanPayBean.setProjectType(EncodingEmun.antCompany.getType());
        scanPayBean.setTerminalNo(ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID));

        Intent intent = new Intent(getActivity(),Network.class);
        intent.putExtra("scan",scanPayBean);
        startActivity(intent);
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
        Intent intent = new Intent(getActivity(), StartScanActivity.class);
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
                        Intent printerIntent = new Intent(getActivity(),PrintResultActivity.class);
                        printerIntent.putExtra("water", water);
                        printerIntent.putExtra("transType", String.valueOf(water.getTransType()));
                        startActivity(printerIntent);
                    }
                }
            }else if (requestCode == TransType.SCAN_POS_CODE){ //扫pos
                ScanPayBean scanPayBean = (ScanPayBean) data.getSerializableExtra("scan_pos");
                if (null != scanPayBean) {  //跳转到生成二维码的界面
                    Intent intent = new Intent(getActivity(), QCImageActivity.class);
                    intent.putExtra("scan_pos", scanPayBean);
                    startActivity(intent);
                }
            }else if (requestCode == Constant.BANK_SIGN  && null != data){  //日切签到
                String transResultTip = data.getStringExtra("transResultTip");
                Intent intent = new Intent(getActivity(), SignSuccessActivity.class);
                intent.putExtra("transResultTip",transResultTip);
                startActivity(intent);
            }
        }else if (resultCode == RESULT_FIRST_USER){
            if (requestCode == Constant.BANK_SIGN){
                startActivity(new Intent(getActivity(),TransErrorResultActivity.class));
            }else {
                startActivity(new Intent(getActivity(),ScanErrorActivity.class));
            }
        }else if (resultCode == RESULT_CANCELED){
            ToastUtils.showToast("交易取消");
        }
    }
}
