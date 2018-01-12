package com.nld.starpos.banktrade.pinUtils;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.pinpad.AidlPinpad;
import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.db.ParamConfigDao;
import com.nld.starpos.banktrade.db.local.ParamConfigDaoImpl;
import com.nld.starpos.banktrade.utils.Constant;

import common.HexUtil;


public class PinpadDev {
    private AidlDeviceService mDeviceService;
    private AidlPinpad mDev;
    private int mType=0;//密码键盘类型
    public PinpadDev(AidlDeviceService service, int type) {
        initDev(service,type);
    }
    
    public boolean initDev(AidlDeviceService service,int type){
        mDeviceService=service;
        mType=type;
        if(null!=service){
            try {
                mDev=AidlPinpad.Stub.asInterface(mDeviceService.getPinPad(mType));
                return true;
            } catch (RemoteException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
    
    /**
     * 主密钥下载
     * @param keyID 密钥索引
     * @param key 密钥值， 明文
     * @param checkvalue 校验值，传 NULL 不需要校验。
     * @throws RemoteException 
     */
    public void loadPinpadMainKey(int keyID ,byte[] key,byte[] checkvalue) throws RemoteException{
        boolean b = mDev.loadMainkey(keyID, key, checkvalue, Constant.IS_SM);
        Log.e("PinpadDev", "主密钥下载 = " + b);
    }

    /**
     * 计算mac
     * @param wkeyid  MAK 索引 ID
     * @param macBlock
     * @return
     * @throws RemoteException
     */
    public byte[] getMac(final int wkeyid,final byte[] macBlock) throws RemoteException{

                Bundle bd=new Bundle();
                bd.putInt("wkeyid", wkeyid);
                bd.putByteArray("data", macBlock);
                bd.putByteArray("random", null);
                bd.putInt("type", 0x01);
                byte[] result=new byte[8];
                int ret=-1;
                try {
                    ret = mDev.getMac(bd, result);
                } catch (RemoteException e) {
                    LogUtils.d("mac计算时出错");
                }
                LogUtils.d("mac计算结果，getMac返回值："+ret+";mac值："+ HexUtil.bcd2str(result));
                if(ret != 0x00){    //mac计算失败
                    result=null;
                }
        return result;
    }

    /**
     * 获取键盘方式
     * @param
     * @return
     */
    public static String getPinPadDevSymbol() {
        ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl();
        return mParamConfigDao.get("pinpadType");
    }
}
