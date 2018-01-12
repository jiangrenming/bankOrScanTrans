package com.nld.starpos.banktrade.thread;

import android.content.Context;
import android.os.Handler;

import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.logger.LogUtils;
import com.nld.netlibrary.https.HttpConnetionHelper;
import com.nld.starpos.banktrade.db.local.ParamConfigDaoImpl;
import com.nld.starpos.banktrade.exception.NldException;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.NldPacketHandle;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.banktrade.utils.TransactionPackageUtil;

import java.util.Map;

import common.HexUtil;
import common.StringUtil;

/**
 * Created by jiangrenming on 2017/12/11.
 * 批上送结束
 */

public class BatchSendEndThread extends ComonThread {


    private AidlDeviceService aidlDeviceService;
    private String uri;
    private NldPacketHandle nldPacketHandle;

    public BatchSendEndThread(Context context, Handler handler,String uri,AidlDeviceService service) {
        super(context, handler);
        this.aidlDeviceService = service;
        this.uri = uri;
        try {
            nldPacketHandle = new NldPacketHandle(context);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e("nldPacketHandle is not initlized");
        }
    }

    @Override
    public byte[] getIsopack() {
        return new byte[0];
    }

    @Override
    public void run() {
        handler.sendMessage(handler.obtainMessage(0x01,"批上送结束"));
        // 上送结束
        Map<String, String> infoEndMap = TransactionPackageUtil.getBatchSendEndInfo(context, null,2);
        byte[] msgData = null;
        try {
            msgData = nldPacketHandle.pack(aidlDeviceService, TransConstans.TRANS_CODE_BATCH_SEND_END, infoEndMap, 1);
        } catch (Exception e1) {
            LogUtils.e("组包过程发生异常"+ "]&&dataMap=[" + infoEndMap + "]");
            e1.printStackTrace();
            Cache.getInstance().setTransCode(NldException.ERR_DAT_PACK_E201);
            Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DAT_PACK_E201));
            handler.sendMessage(handler.obtainMessage(0x02));
        }
        byte[] msg = nldPacketHandle.addMessageLen(msgData);
        LogUtils.d("发送加签的报文=[" + HexUtil.bcd2str(msg) + "]");
        try{
            String resultStr = HttpConnetionHelper.httpClient.postHttpsUrl(uri, HexUtil.bcd2str(msg),handler);
            if (null == resultStr) {  //网络访问失败
                Cache.getInstance().setErrCode(NldException.ERR_NET_DEFAULT_E102);
                Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_NET_DEFAULT_E102));
                handler.sendMessage(handler.obtainMessage(0x02));
            }
            LogUtils.d("接收前置返回的交易数据：[" + resultStr + "]");
            byte[] pack = nldPacketHandle.subMessLen(HexUtil.hexStringToByte(resultStr));
            LogUtils.d("返回的报文=[" + HexUtil.bcd2str(pack) + "]");
            Map<String, String> resmap = nldPacketHandle.unPack(pack, "002311", 1);//解包
            LogUtils.d("解包后resmap " + StringUtil.map2LineStr(resmap)+"/39域 rescode ="+ resmap.get("respcode"));
            ParamsUtil.getInstance().update("upbillnosymbol", "1000");
            if ("00".equals(resmap.get("respcode"))) {
                resmap.put("respcode","95");
                String field63 = new ParamConfigDaoImpl().get("requestSettleData");
                resmap.put("field63", field63);
                Cache.getInstance().setResultMap(resmap);
                handler.sendMessage(handler.obtainMessage(0x03));
            } else {
                Cache.getInstance().setResultMap(resmap);
                handler.sendMessage(handler.obtainMessage(0x02));
            }
        }catch (Exception e){
            e.printStackTrace();
            LogUtils.d("批上送异常：" + e.getMessage());
            String expcode = NldException.getExpCode(e, NldException.ERR_NET_DEFAULT_E102);
            Cache.getInstance().setErrCode(expcode);
            Cache.getInstance().setErrDesc(NldException.getMsg(expcode));
            handler.sendMessage(handler.obtainMessage(0x02));
        }
    }
}
