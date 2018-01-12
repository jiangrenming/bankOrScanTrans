package com.nld.cloudpos.payment.controller;

import android.content.Intent;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.scan.AidlScanner;
import com.nld.cloudpos.aidl.scan.AidlScannerListener;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.data.ScanConstant;
import com.nld.cloudpos.util.CommonContants;
import com.nld.starpos.wxtrade.activity.StartScanActivity;
import com.nld.starpos.wxtrade.bean.scan_common.ScanCache;
import com.nld.starpos.wxtrade.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.utils.ShareScanPreferenceUtils;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import com.nld.starpos.wxtrade.utils.params.EncodingEmun;
import com.nld.starpos.wxtrade.utils.params.ScanTransFlagUtil;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;
import com.nld.starpos.wxtrade.utils.params.TransType;

import common.StringUtil;

/**
 * Created by jiangrenming on 2017/9/29.
 * 扫码查单
 */

public class ScanQueryOrderActivity  extends AbstractActivity implements View.OnClickListener{

    private AidlDeviceService mService;
    @ViewInject(R.id.et_querycode)
    EditText et_querycode;
    @ViewInject(R.id.tv_confirm)
    TextView tv_confirm;
    @ViewInject(R.id.img_scan)
    ImageView img_scan;

    @Override
    public int contentViewSourceID() {
        return R.layout.scan_refund_auth;
    }

    @Override
    public void initView() {
        ViewUtils.inject(this);
        img_scan.setOnClickListener(this);
        tv_confirm.setOnClickListener(this);
        setTopDefaultReturn();
    }

    @Override
    public void initData() {
        setTopTitle("扫码查单");
        et_querycode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int action, KeyEvent keyEvent) {
                if (action == EditorInfo.IME_ACTION_DONE
                        || action == EditorInfo.IME_ACTION_SEND
                        ||action == EditorInfo.IME_ACTION_NEXT
                        || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String order =et_querycode.getText().toString().trim();
                    if(StringUtil.isEmpty(order)){
                        ToastUtils.showToast("请输入订单号！");
                        return true;
                    }
                    if(!StringUtil.isEmpty(order)){
                        startQueryActivity(order);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public void startQueryActivity(String order) {
        ScanPayBean auth = getRefundBean();
        auth.setLogNo(order);
        Intent intent = new Intent(ScanQueryOrderActivity.this, StartScanActivity.class);
        intent.putExtra("scan",auth);
        startActivityForResult(intent, TransType.SCAN_PAY_QUERY_CODE);
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
                hideSystemKeyBoard(ScanQueryOrderActivity.this,et_querycode);
                final ScanPayBean auth = getRefundBean();

                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        //打开扫码
                        int scannner= ScanConstant.ScanType.FRONT;
                        int scannerType = ShareScanPreferenceUtils.getInt(ScanQueryOrderActivity.this, TransParamsValue.PARAMS_KEY_SCANNER, 1);
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
                                        auth.setLogNo(strings[0]);  //扫码得到的订单号
                                    }

                                    @Override
                                    public void onFinish() throws RemoteException {
                                        Log.i("TAG","扫码退货");
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (auth.isSucess()){
                                                    String logNo = auth.getLogNo();
                                                    if (!StringUtil.isEmpty(logNo)){
                                                        et_querycode.setText(logNo);
                                                        hideSystemKeyBoard(ScanQueryOrderActivity.this,et_querycode);
                                                    }
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
                String order =et_querycode.getText().toString().trim();
                if(StringUtil.isEmpty(order)){
                    ToastUtils.showToast("请输入订单号！");
                    return;
                }
                if (!StringUtil.isEmpty(order)){
                    startQueryActivity(order);
                }
                break;
            default:
                break;
        }
    }

    @NonNull
    public ScanPayBean getRefundBean() {
        ScanCache.getInstance().setTransCode(ScanTransFlagUtil.TRANS_CODE_WX_QUERY);
        ScanPayBean auth = new ScanPayBean();
        auth.setTransType(TransParamsValue.AntCompanyInterfaceType.SCAN_QUER); //接口
        auth.setRequestUrl(CommonContants.url);
        auth.setProjectType(EncodingEmun.antCompany.getType()); //项目类别
        return auth;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == TransType.SCAN_PAY_QUERY_CODE){
                if (data != null){
                    ScanPayBean scan_query = (ScanPayBean) data.getSerializableExtra("scan_query");
                    if (scan_query != null){
                        Intent i = new Intent(ScanQueryOrderActivity.this,ScanQueryResultActivity.class);
                        i.putExtra("scan_query",scan_query);
                        startActivity(i);
                    }
                }
            }
        }else if (resultCode == RESULT_FIRST_USER){
            Intent intent = new Intent(ScanQueryOrderActivity.this,ScanErrorActivity.class);
            startActivity(intent);
        }else if (resultCode == RESULT_CANCELED){
            ToastUtils.showToast(getString(R.string.no_query_data));
        }
    }
}
