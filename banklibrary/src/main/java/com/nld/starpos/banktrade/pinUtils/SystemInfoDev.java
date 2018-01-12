package com.nld.starpos.banktrade.pinUtils;

import android.content.Context;
import android.os.RemoteException;

import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.system.AidlSystem;
import com.nld.cloudpos.aidl.system.InstallAppObserver;
import com.nld.logger.LogUtils;

import java.util.Calendar;


public class SystemInfoDev {
    private Context mContext;
    private static SystemInfoDev mInstance=null;
    private static AidlSystem mSystem=null;
    private SystemInfoDev(Context context){
        mContext=context;
    }
    public static SystemInfoDev getInstance(Context context, AidlDeviceService aidlService){
        if(null==mInstance){
            mInstance=new SystemInfoDev(context);
        }
        try {
            mSystem=AidlSystem.Stub.asInterface(aidlService.getSystemService());
        } catch (RemoteException e) {
            LogUtils.e("获取系统信息失败");
            e.printStackTrace();
        }
        return mInstance;
    }
    
    /**
     * 获取终端序列号
     * @return
     */
    public String getTermSN(){
        String sn="";
        try {
            if(null==mSystem){
                sn="";
                return "";
            }
            sn= mSystem.getSerialNo();
        } catch (RemoteException e) {
            LogUtils.e("获取终端序列号失败");
            sn="";
            e.printStackTrace();
            return sn;
        }
//        sn="YP760000000004";
        return sn;
    }
    
    /**
     * 获取系统IMSI
     * @return
     */
    public String getIMSI(){
        String imsi="";
        try {
            imsi=mSystem.getIMSI();
        } catch (RemoteException e) {
            LogUtils.e("获取IMSI失败");
            imsi="";
            e.printStackTrace();
            return imsi;
        }
        return imsi;
    }

    /**
     * 获取型号
     * @return
     */
    public String getModel(){
        String imsi="";
        try {
            imsi=mSystem.getModel();
        } catch (RemoteException e) {
            LogUtils.e("获取IMSI失败");
            imsi="";
            e.printStackTrace();
            return imsi;
        }
        return imsi;
    }

    /**
     * 获取厂商
     * @return
     */
    public String getManufacture(){
        String imsi="";
        try {
            imsi=mSystem.getManufacture();
        } catch (RemoteException e) {
            LogUtils.e("获取IMSI失败");
            imsi="";
            e.printStackTrace();
            return imsi;
        }
        return imsi;
    }
    

    /**
     * 获取系统IMEI
     * @return
     */
    public String getIMEI(){
        String imsi="";
        try {
            imsi=mSystem.getIMEI();
        } catch (RemoteException e) {
            LogUtils.e("获取IMSI失败");
            imsi="";
            e.printStackTrace();
            return imsi;
        }
        return imsi;
    }
    
    /**
     * 获取安装包路径
     * @return
     */
    public String getInstallPath(){
        String path="";
        try {
            path=mSystem.getStoragePath();
        } catch (RemoteException e) {
            LogUtils.e("获取安装包存储路径失败"+e);
            path="";
            e.printStackTrace();
        }
        return path;
    }
    
    /**
     * 安装应用
     * @param path
     * @param listener
     * @return
     */
    public boolean installApk(String path,InstallAppObserver listener){
        try {
            LogUtils.i("调用接口安装apk");
            mSystem.installApp(path, listener);
        } catch (RemoteException e) {
            LogUtils.e("安装apk失败："+path);
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    /**
     * 驱动更新
     * @return
     */
    public boolean installDriver(){
        try {
            mSystem.update(0x01);
            return true;
        } catch (RemoteException e) {
            LogUtils.e("调用驱动更新接口异常");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 更新系统时间
     * @param datetime  MMddhhmmss
     * @return
     */
    public boolean setDatetime(String datetime){
        boolean success=false;
        Calendar c = Calendar.getInstance();   
        String year = String.valueOf(c.get(Calendar.YEAR));
        try {
            success= mSystem.updateSysTime(year+datetime);
        } catch (RemoteException e) {
            LogUtils.d("更新系统时间失败",e);
            e.printStackTrace();
        }
        return success;
    }
}
