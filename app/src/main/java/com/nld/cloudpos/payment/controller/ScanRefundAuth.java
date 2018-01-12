package com.nld.cloudpos.payment.controller;

import android.content.Intent;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.scan.AidlScanner;
import com.nld.cloudpos.aidl.scan.AidlScannerListener;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.data.ScanConstant;
import com.nld.cloudpos.payment.activity.Network;
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

import java.text.DecimalFormat;

import common.DateTimeUtil;
import common.StringUtil;

/**
 * Created by jiangrenming on 2017/9/28.
 * 扫码退货界面
 */

public class ScanRefundAuth extends AbstractActivity implements View.OnClickListener{

    private AidlDeviceService mService;
    @ViewInject(R.id.et_querycode)
    EditText et_querycode;
    @ViewInject(R.id.tv_confirm)
    TextView tv_confirm;
    @ViewInject(R.id.img_scan)
    ImageView img_scan;
    @ViewInject(R.id.ll_refund_amt)
    LinearLayout ll_refund_amt;
    @ViewInject(R.id.et_refund_amt)
    EditText et_refund_amt;

    @Override
    public int contentViewSourceID() {
        return R.layout.scan_refund_auth;
    }

    @Override
    public void initView() {
        ViewUtils.inject(this);
        img_scan.setOnClickListener(this);
        tv_confirm.setOnClickListener(this);
        ll_refund_amt.setVisibility(View.VISIBLE);
        setTopDefaultReturn();
    }

    @Override
    public void initData() {
        setTopTitle("扫码退货");

        if (ll_refund_amt.getVisibility() != View.VISIBLE){
            et_querycode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int action, KeyEvent keyEvent) {
                    if (action == EditorInfo.IME_ACTION_DONE
                            || action == EditorInfo.IME_ACTION_SEND
                            ||action == EditorInfo.IME_ACTION_NEXT
                            || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                        String order =et_querycode.getText().toString().trim();
                        if(StringUtil.isEmpty(order)){
                            ToastUtils.showToast(getString(R.string.str_input_order_num_please));
                            return true;
                        }
                        ScanPayBean auth = getRefundBean();
                        auth.setOrderNo(order);
                        auth.setAmount(null);
                        Intent intent = new Intent(ScanRefundAuth.this, Network.class);
                        intent.putExtra("scan_refund",auth);
                        startActivity(intent);
                        return true;
                    }
                    return false;
                }
            });
        }else {
            et_refund_amt.setFilters(new InputFilter[]{new AmountInputFilter()});
            et_querycode.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            et_querycode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int action, KeyEvent keyEvent) {
                    if (action == EditorInfo.IME_ACTION_NEXT) {
                        if (TextUtils.isEmpty(et_querycode.getText().toString().trim())) {
                            ToastUtils.showToast(getString(R.string.str_input_order_num_please));
                        } else {
                            et_refund_amt.setFocusable(true);
                            et_refund_amt.setFocusableInTouchMode(true);
                            et_refund_amt.requestFocus();
                        }
                        return true;
                    }else if (action == EditorInfo.IME_ACTION_DONE){
                        ScanPayBean auth = getRefundBean();
                        if (comfirmOrder(auth)){
                            gotoNextActivity(auth);
                        }
                        return true;
                    }
                    return false;
                }
            });

            et_refund_amt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int action, KeyEvent keyEvent) {
                    if (action == EditorInfo.IME_ACTION_DONE) {
                        ScanPayBean auth = getRefundBean();
                        if (comfirmOrder(auth)){
                            gotoNextActivity(auth);
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
    }
    /**
     * 跳转界面
     * @param auth
     */
    private void gotoNextActivity(ScanPayBean auth) {

        ScanCache.getInstance().setTransCode(ScanTransFlagUtil.TRANS_CODE_WX_TH);
        Intent intent = new Intent(ScanRefundAuth.this, StartScanActivity.class);
        intent.putExtra("scan",auth);
        startActivityForResult(intent, TransType.SCAN_PAY_REFUND_CODE);
    }


    private boolean comfirmOrder(ScanPayBean auth){
        boolean isConfirmed = false;
        String order = et_querycode.getText().toString().trim();
        if (StringUtil.isEmpty(order)){
            ToastUtils.showToast(getString(R.string.str_input_order_num_please));
        }else {
            auth.setOrderNo(order);
            String refundAmt = et_refund_amt.getText().toString().trim();
            if (StringUtil.isEmpty(refundAmt)){
                auth.setAmount(null);
                isConfirmed = true;
            }else {
                try{
                    long parseAmt = Long.parseLong(new DecimalFormat("##0.00")
                            .format(Double.parseDouble(refundAmt))
                            .replace(".", ""));
                    if (parseAmt >0L){
                        auth.setAmount(parseAmt);
                        isConfirmed = true;
                    }else {
                        ToastUtils.showToast(getString(R.string.str_input_tk_money_zero));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    ToastUtils.showToast(getString(R.string.str_input_right_tk_money));
                    isConfirmed = false;
                }
            }
        }
        return isConfirmed;
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
        switch (view.getId()){
            case R.id.img_scan:
                hideSystemKeyBoard(ScanRefundAuth.this,et_querycode);
                final ScanPayBean auth = new ScanPayBean();

                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        //打开扫码
                        int scannner= ScanConstant.ScanType.FRONT;
                        int scannerType = ShareScanPreferenceUtils.getInt(ScanRefundAuth.this, TransParamsValue.PARAMS_KEY_SCANNER, 1);
                        if (scannerType == 0){  //前置
                            scannner  = ScanConstant.ScanType.FRONT;
                        }else {
                            scannner  = ScanConstant.ScanType.BACK;
                        }
                        try {
                            if (mService != null){
                                AidlScanner mServiceScanner =  AidlScanner.Stub.asInterface(mService.getScanner());
                                mServiceScanner.startScan(scannner, 60, new AidlScannerListener.Stub(){
                                    @Override
                                    public void onScanResult(String[] strings) throws RemoteException {
                                        Log.i("TAG","扫码成功"+strings[0]);
                                        auth.setSucess(true);
                                        auth.setOrderNo(strings[0]);  //扫码得到的订单号
                                    }

                                    @Override
                                    public void onFinish() throws RemoteException {
                                        Log.i("TAG","扫码退货");
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (ll_refund_amt.getVisibility() != View.VISIBLE){
                                                    auth.setAmount(null);
                                                    Intent intent = new Intent(ScanRefundAuth.this, Network.class);
                                                    intent.putExtra("scan_refund",auth);
                                                    startActivity(intent);
                                                }else {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (auth.isSucess()){
                                                                String orderNo = auth.getOrderNo();
                                                                if (!StringUtil.isEmpty(orderNo)){
                                                                    et_querycode.setText(orderNo);
                                                                    et_refund_amt.setFocusable(true);
                                                                    et_refund_amt.setFocusableInTouchMode(true);
                                                                    et_refund_amt.requestFocus();
                                                                }
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(int i, String s) throws RemoteException {
                                        ToastUtils.showToast(s);
                                        auth.setSucess(false);
                                    }
                                });
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            ToastUtils.showToast(e.toString());
                            auth.setSucess(false);
                        }
                    }
                }.start();
                break;
            case R.id.tv_confirm:
              /*  String order =et_querycode.getText().toString().trim();
                if(StringUtil.isEmpty(order)){
                    ToastUtils.showToast("请输入订单号！");
                    return;
                }
                if (!StringUtil.isEmpty(order)){
                    startRefundActivity(order);
                }*/
                if (ll_refund_amt.getVisibility() != View.VISIBLE){
                    String order =et_querycode.getText().toString().trim();
                    if(StringUtil.isEmpty(order)){
                        ToastUtils.showToast(getString(R.string.str_input_order_num_please));
                        return;
                    }
                    if (!StringUtil.isEmpty(order)){
                        ScanPayBean refundBean = getRefundBean();
                        refundBean.setAmount(null);
                        refundBean.setOrderNo(order);
                        gotoNextActivity(refundBean);
                    }
                }else {
                    ScanPayBean refundBean = getRefundBean();
                    if (comfirmOrder(refundBean)){
                        gotoNextActivity(refundBean);
                    }
                }
                break;
            default:
                break;
        }
    }

    public void startRefundActivity(String order) {
        ScanCache.getInstance().setTransCode(ScanTransFlagUtil.TRANS_CODE_WX_TH);
        ScanPayBean refundBean = getRefundBean();
        refundBean.setOrderNo(order);
        Intent intent = new Intent(ScanRefundAuth.this, StartScanActivity.class);
        intent.putExtra("scan",refundBean);
        startActivityForResult(intent, TransType.SCAN_PAY_REFUND_CODE);
    }

    @NonNull
    public ScanPayBean getRefundBean() {
        ScanPayBean auth = new ScanPayBean();
        auth.setTransType(TransParamsValue.AntCompanyInterfaceType.SCAN_REFUND); //接口
        String posSn = ShareScanPreferenceUtils.getString(this, CommonParams.POSSn, null);
        if (!StringUtil.isEmpty(posSn)) {
            auth.setSn(ShareScanPreferenceUtils.getString(ScanRefundAuth.this, CommonParams.POSSn,null));  //终端序列号
        }
        auth.setTerminalNo(ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID)); //终端号
        auth.setTxnCnl(TransType.ScanTransType.TRANS_SCAN_PAY_I); //交易渠道
        auth.setOrderId(ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_SYSTRANCE_NO)); //流水号
        auth.setBatchNo(ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_TYANS_BATCHNO)); //批次号
        auth.setType(TransType.ScanTransType.TRANS_SCAN_REFUND); //交易类型
        auth.setCurrency(EncodingEmun.CNYCURRENCY.getType()); //币种
        auth.setDate(DateTimeUtil.getCurrentDate()); //日期
        auth.setTime(DateTimeUtil.getCurrentTime());  //时间
        auth.setYear(DateTimeUtil.getCurrentDate(DateTimeUtil.YYYY)); //年份
        auth.setPayChannel(TransType.ScanTransType.TRANS_SCAN_REFUND_CHANNCLE); //支付渠道
        auth.setRequestUrl(CommonContants.url);
        auth.setProjectType(EncodingEmun.antCompany.getType());
        return auth;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == TransType.SCAN_PAY_REFUND_CODE){
                if (data != null){
                    ScanTransRecord water = (ScanTransRecord) data.getExtras().getSerializable("water");
                    if (water !=null){
                        Intent intent = new Intent(ScanRefundAuth.this, PrintResultActivity.class);
                        intent.putExtra("water", water);
                        intent.putExtra("transType", String.valueOf(water.getTransType()));
                        startActivity(intent);
                    }
                }
            }
        }else if (resultCode == RESULT_FIRST_USER){
            Intent intent = new Intent(ScanRefundAuth.this,ScanErrorActivity.class);
            startActivity(intent);
        }else if (resultCode == RESULT_CANCELED){
            ToastUtils.showToast(getString(R.string.no_water_data));
        }
    }
}
