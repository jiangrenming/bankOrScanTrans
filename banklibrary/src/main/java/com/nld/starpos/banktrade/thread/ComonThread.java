package com.nld.starpos.banktrade.thread;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.nld.starpos.banktrade.bean.ResultStatus;
import com.nld.starpos.banktrade.exception.NldException;

import java.text.SimpleDateFormat;

/**
 * Created by jiangrenming on 2017/10/30.
 */

public abstract  class ComonThread implements Runnable{

    protected SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss");

    protected Context context;
    protected Handler handler;

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


    public void sendEmptyMessage(int what) {
        if (!isCancel) {
            handler.sendEmptyMessage(what);
        } else {
        }

    }

    public void sendMessage(Message msg) {
        if (!isCancel) {
            handler.sendMessage(msg);
        } else {
        }
    }

    /**
     * @return
     * @description 获取8583报文
     */
    public abstract byte[] getIsopack();


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
            e.printStackTrace();
        }
    }

    public void cancel() {
        isCancel = true;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
