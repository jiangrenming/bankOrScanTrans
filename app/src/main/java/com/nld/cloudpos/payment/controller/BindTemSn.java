package com.nld.cloudpos.payment.controller;

import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.pinpad.AidlPinpad;
import com.nld.cloudpos.aidl.pinpad.TusnData;
import com.nld.cloudpos.aidl.system.AidlSystem;
import com.nld.starpos.wxtrade.utils.ToastUtils;

/**
 * Created by jiangrenming on 2017/9/19.
 */

public class BindTemSn {


    //CSN为序列号
    public static String getCSN(AidlDeviceService serviceManager){
        try{
            AidlSystem systemInf = AidlSystem.Stub.asInterface(serviceManager.getSystemService());
            String CSN = systemInf.getSerialNo(); //SN号
            if (TextUtils.isEmpty(CSN)) {
                ToastUtils.showToast("没有获取到CSN号");
                return null;
            }
           return  CSN;
        }catch (Exception e){
            e.printStackTrace();
        }
        return  null;
    }
    //SN为终端号
    public static String getSN(AidlDeviceService serviceManager){
        Log.d("TAG","获取设备序列号sn");
        String sn = null;
        try {
            AidlPinpad pad = AidlPinpad.Stub.asInterface(serviceManager.getPinPad(1));
            TusnData tusnData = pad.getTusnData(null);
            if (tusnData != null) {
                String tempSn = tusnData.getSn();
                if (!TextUtils.isEmpty(tempSn)) {
                    sn = tempSn;
                }
            }
        } catch (RemoteException e) {
            Log.d("TAG","获取设备序列号sn时断开了远程连接");
        }
        return sn;
    }
}
