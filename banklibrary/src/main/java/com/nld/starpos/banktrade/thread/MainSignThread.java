package com.nld.starpos.banktrade.thread;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.logger.LogUtils;
import com.nld.netlibrary.https.HttpConnetionHelper;
import com.nld.starpos.banktrade.exception.NldException;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.NldPacketHandle;
import com.nld.starpos.banktrade.utils.ParamsConts;
import com.nld.starpos.banktrade.utils.ParamsUtil;

import java.util.Map;

import common.HexUtil;

/**
 * Created by jiangrenming on 2017/12/12.
 * 签到
 */

public class MainSignThread extends ComonThread {

    private String mTransCode;
    private Map<String,String> signMap;
    private NldPacketHandle nldPacketHandle;
    private String url;
    private AidlDeviceService aidlDeviceService;

    public MainSignThread(Context context, Handler handler,String transCode
            ,Map<String,String> sign,String uri,AidlDeviceService service) {
        super(context, handler);
        this.mTransCode = transCode;
        this.signMap = sign;
        this.url = uri;
        this.aidlDeviceService = service;
        try {
            nldPacketHandle = new NldPacketHandle(context);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e("nldPacketHandle is not initlized");
        }
    }


    @Override
    public void run() {
        byte[] packMsg = getPack(signMap, mTransCode);
        try {
            String resultStr = HttpConnetionHelper.httpClient.postHttpsUrl(url, HexUtil.bcd2str(packMsg),handler);
            if (resultStr.isEmpty()) {
                Cache.getInstance().setErrCode(NldException.ERR_NET_DEFAULT_E102);
                Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_NET_DEFAULT_E102));
                Message errorEmessage = handler.obtainMessage(0x11);
                sendMessage(errorEmessage);
            }
            LogUtils.d("接收前置返回的交易数据：[" + resultStr + "]");
            String processRequire = resultStr.substring(19, 20);
            LogUtils.d("报文头处理要求：" + processRequire);
            if (processRequire.equals("6")) {
                //需要更新TMS参数
                ParamsUtil.getInstance().update("dowmloadparam", "6");
            } else if (processRequire.equals("4")) {
                //需要下载公钥
                ParamsUtil.getInstance().update(ParamsConts.UPDATA_STATUS, "1");
            } else if (processRequire.equals("5")) {
                //需要下载IC卡参数
                ParamsUtil.getInstance().update(ParamsConts.UPDATA_STATUS, "2");
            }
            byte[] pack = nldPacketHandle.subMessLen(HexUtil.hexStringToByte(resultStr));
            LogUtils.d("返回的报文=[" + HexUtil.bcd2str(pack) + "]");
            Map<String, String> resmap = nldPacketHandle.unPack(pack, "002311", 1);//解包
            Cache.getInstance().setResultMap(resmap);
            if ("00".equals(resmap.get("respcode"))) {
                LogUtils.d("签到交易返回MAP: " + resmap);
               handler.sendMessage(handler.obtainMessage(0x03, "签到成功")) ;
            } else {            //接到数据，但是交易处理不成功，不进行mac校验
                Cache.getInstance().setErrCode(resmap.get("respcode"));
                String tip = NldException.getMsg(resmap.get("respcode"), NldException.MSG_RECEIVE_ERR);
                Cache.getInstance().setErrDesc(tip);
                handler.sendMessage(handler.obtainMessage(0x02));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Cache.getInstance().setErrCode(NldException.ERR_NET_DEFAULT_E102);
            String expcode = NldException.getExpCode(e, NldException.ERR_NET_DEFAULT_E102);
            Cache.getInstance().setErrDesc(expcode);
            handler.sendMessage(handler.obtainMessage(0x02));
        }
    }


    //组包
    private byte[] getPack(Map<String, String> dataMap, String transCode) {

        LogUtils.d("组包前的msgData=" + dataMap.toString());
        byte[] msgData = null;
        try {
            msgData = nldPacketHandle.pack(aidlDeviceService, transCode, dataMap, 1);
            System.out.println("dataMap的内容为：");
            System.out.println(dataMap.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Cache.getInstance().setErrCode(NldException.ERR_DAT_PACK_E201);
            Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DAT_PACK_E201));
            handler.sendMessage( handler.obtainMessage(0x02));
        }
        byte[] msg = nldPacketHandle.addMessageLen(msgData);
        System.out.println("交易mct编码：" + transCode);
        System.out.println("请求发送的报文=[" + HexUtil.bcd2str(msg) + "]");
        return msg != null ? msg : null;
    }
    @Override
    public byte[] getIsopack() {
        return new byte[0];
    }
}
