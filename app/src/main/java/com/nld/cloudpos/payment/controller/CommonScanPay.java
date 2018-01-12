package com.nld.cloudpos.payment.controller;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.nld.cloudpos.BankApplication;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.scan.AidlScanner;
import com.nld.cloudpos.aidl.scan.AidlScannerListener;
import com.nld.cloudpos.data.ScanConstant;
import com.nld.starpos.wxtrade.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.utils.ShareScanPreferenceUtils;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;

/**
 * Created by jiangrenming on 2017/9/20.
 */

public class CommonScanPay {

    private int mTransType;
    private AidlDeviceService mAidlService;
    private AidlScanner aidlScanner;
    private int scannner = ScanConstant.ScanType.FRONT;
    private Context context;
    private ScanPayBean scanPayBean;
    private Handler handler;

    public CommonScanPay(int transType, Context context, ScanPayBean bean){
        this.mTransType = transType;
        this.context = context;
        this.scanPayBean = bean;
        mAidlService = BankApplication.mDeviceService;
        if ( null != mAidlService ){
            try{
                aidlScanner =  AidlScanner.Stub.asInterface(mAidlService.getScanner());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public CommonScanPay(int transType, Context context, ScanPayBean bean, Handler mHandle){
        this.handler = mHandle;
        this.mTransType = transType;
        this.context = context;
        this.scanPayBean = bean;
        mAidlService = BankApplication.mDeviceService;
        if ( null != mAidlService ){
            try{
                aidlScanner =  AidlScanner.Stub.asInterface(mAidlService.getScanner());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void scan_pay(){
        if (null != aidlScanner){
            int scannerType = ShareScanPreferenceUtils.getInt(context, TransParamsValue.PARAMS_KEY_SCANNER, 1);
            if (scannerType == 0){  //前置
                scannner  = ScanConstant.ScanType.FRONT;
            }else {
                scannner  = ScanConstant.ScanType.BACK;
            }
            try{
                aidlScanner.startScan(scannner, 60, new AidlScannerListener.Stub() {
                    @Override
                    public void onScanResult(String[] strings) throws RemoteException {
                        Log.i("TAG","扫码成功"+strings[0]);
                        scanPayBean.setSucess(true);
                        scanPayBean.setScanResult(strings[0]);
                    }

                    @Override
                    public void onFinish() throws RemoteException {
                        Log.i("TAG","扫码结算");
                        if (scanPayBean.isSucess()){
                            Message message = Message.obtain();
                            message.what = 0x11;
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("scan",scanPayBean);
                            message.setData(bundle);
                            handler.sendMessage(message);
                        }else {
                            return;
                        }
                    }

                    @Override
                    public void onError(int i, String s) throws RemoteException {
                        ToastUtils.showToast(s);
                        scanPayBean.setSucess(false);
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
                scanPayBean.setSucess(false);
                ToastUtils.showToast(e.toString());
            }
        }
    }

}
