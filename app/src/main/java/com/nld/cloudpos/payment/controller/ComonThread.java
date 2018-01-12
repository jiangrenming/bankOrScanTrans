package com.nld.cloudpos.payment.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.pinpad.AidlPinpad;
import com.nld.cloudpos.util.MyLog;
import com.nld.cloudpos.util.ShareUtil;
import com.nld.starpos.banktrade.bean.ResultStatus;
import com.nld.starpos.banktrade.exception.NldException;
import com.nld.starpos.banktrade.utils.Constant;

import java.text.SimpleDateFormat;
import java.util.Map;

import common.HexUtil;
import common.StringUtil;

public abstract class ComonThread implements Runnable {

    private static MyLog logger = MyLog.getLogger(ComonThread.class);

    protected SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss");

    protected Context context;
    protected Handler handler;

    protected AidlDeviceService deviceService;

    protected boolean isCancel = false;
    public String mOpearcode;
    public int ipParamtime = 0;
    public int catTime = 0;

    public void setOperCode(String opearcode) {
        this.mOpearcode = opearcode;
    }

    public void setIpParamtime(int ipParamtime) {
        this.ipParamtime = ipParamtime;
    }

    public void setCatTime(int catTime) {
        this.catTime = catTime;
    }


    public ComonThread(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setDeviceService(AidlDeviceService deviceService) {
        this.deviceService = deviceService;
    }

    public void sendEmptyMessage(int what) {
        if (!isCancel) {
            handler.sendEmptyMessage(what);
        } else {
            logger.d("Handler被取消，不发送信息");
        }
    }

    public void sendMessage(Message msg) {
        if (!isCancel) {
            handler.sendMessage(msg);
        } else {
            logger.d("Handler被取消，不发送信息");
        }
    }

    /**
     * @return
     * @description 获取8583报文
     */
    public abstract byte[] getIsopack();

    public void updateWkey(Map<String, String> map) {
        String field44 = map.get("field44");
        logger.log(MyLog.LogType.DEBUG, "field44 --> " + field44);
        if (!StringUtil.isEmpty(field44)) {
            String pinkey = field44.substring(0, 32);
            String mackey = field44.substring(32, 48);
            mackey = mackey + mackey;
            String pinpadType = ShareUtil.getAppConfig("pinpadType", "0");
            logger.log(MyLog.LogType.TEST, "pinkey: " + pinkey);
            logger.log(MyLog.LogType.TEST, "mackey: " + mackey);
            try {
                AidlPinpad dev = AidlPinpad.Stub.asInterface(deviceService
                        .getPinPad(pinpadType.equals("0") ? 0 : 1));
                String mkeyId = ShareUtil.getAppConfig(Constant.FIELD_WX_NEW_MKEY_ID, "2");
                String pikId = ShareUtil.getAppConfig(Constant.FIELD_WX_NEW_PIK_ID, "2");
                String makId = ShareUtil.getAppConfig(Constant.FIELD_WX_NEW_MAK_ID, "2");
                logger.log(MyLog.LogType.INFO, "微信密钥索引：mkeyId:" + mkeyId + "  pikId:" + pikId + "  makId:" + makId);
                int iPik, iMak, iMKey;
                iPik = Integer.parseInt(pikId);
                iMak = Integer.parseInt(makId);
                iMKey = Integer.parseInt(mkeyId);
                // PIK
                boolean loadPik = dev.loadWorkKey(0x01, iMKey, iPik,
                        HexUtil.hexStringToByte(pinkey), null, Constant.IS_SM);
                // MAK
                boolean loadMak = dev.loadWorkKey(0x03, iMKey, iMak,
                        HexUtil.hexStringToByte(mackey), null, Constant.IS_SM);
                if (!loadPik) {
                    logger.log(MyLog.LogType.WARN, "注入pinkey异常 -->" + pinkey);
                    return;
                }
                logger.log(MyLog.LogType.INFO, "注入pinkey成功 -->" + pinkey);
                if (!loadMak) {
                    logger.log(MyLog.LogType.WARN, "注入mackey异常 -->" + mackey);
                    return;
                }
                logger.log(MyLog.LogType.INFO, "注入mackey成功 -->" + mackey);
            } catch (RemoteException e) {
                e.printStackTrace();
                logger.e("微信交易密钥注入失败", e);
            } catch (Exception e) {
                e.printStackTrace();
                logger.e("微信交易密钥注入失败", e);
            }
        }
    }

    /**
     * @param message
     * @return
     * @description 去除两字节报文长度
     */
    protected byte[] subMessLen(byte[] message) {
        byte[] msg = new byte[message.length - 2];
        System.arraycopy(message, 2, msg, 0, msg.length);
        return msg;
    }

    /**
     * @param message
     * @return
     * @description 添加两字节 报文长度
     */
    public byte[] addMessageLen(byte[] message) {
        int iLen = message.length;
        byte[] targets = new byte[]{(byte) (iLen / 256), (byte) (iLen % 256)};
        byte[] msg = new byte[iLen + 2];
        System.arraycopy(targets, 0, msg, 0, 2); // 拷贝长度
        System.arraycopy(message, 0, msg, 2, iLen); // 拷贝报文
        return msg;
    }

    /**
     * 处理异常
     */
    public void dealException(Exception exp, ResultStatus result) {
        try {
            String retCode = NldException.getExpCode(exp, NldException.ERR_NET_DEFAULT_E102);
            result.setRetCode(retCode);
            result.setErrMsg(NldException.getMsg(retCode));
        } catch (Exception e) {
            logger.e("处理异常时发生错误..", e);
            e.printStackTrace();
        }
    }

    public void cancel() {
        logger.d("线程取消");
        isCancel = true;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
