package com.nld.starpos.banktrade.thread;

import android.content.Context;
import android.os.Handler;

import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.logger.LogUtils;
import com.nld.netlibrary.https.HttpConnetionHelper;
import com.nld.starpos.banktrade.db.TransRecordDao;
import com.nld.starpos.banktrade.db.bean.TransRecord;
import com.nld.starpos.banktrade.db.local.TransRecordDaoImpl;
import com.nld.starpos.banktrade.exception.NldException;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.NldPacketHandle;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.banktrade.utils.TransactionPackageUtil;

import java.util.List;
import java.util.Map;

import common.HexUtil;
import common.StringUtil;

/**
 * Created by jiangrenming on 2017/12/11.
 * IC卡交易批上送线程
 */

public class ICCardBatchSendThread extends ComonThread {

    private AidlDeviceService aidlDeviceService;
    private String uri;
    private NldPacketHandle nldPacketHandle;
    private TransRecordDao mTransRecordDao;
    public static final String SEND_SUCC = "5";//批结对账不平 单笔批上送成功

    public ICCardBatchSendThread(Context context, Handler handler,AidlDeviceService service,String uri) {
        super(context, handler);
        this.uri = uri;
        this.aidlDeviceService = service;
        try {
            mTransRecordDao = new TransRecordDaoImpl();
            nldPacketHandle = new NldPacketHandle(context);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e("nldPacketHandle is not initlized");
        }

    }

    @Override
    public void run() {
        // 批上送IC卡交易
        List<TransRecord> transRecordICList = mTransRecordDao.getICUnBatchSendSucc();
        int requestCount = 1;//上送次数
        if (transRecordICList != null && transRecordICList.size() > 0) {
            for (TransRecord transRecord : transRecordICList) {
                String reserve5 = transRecord.getReserve5();
                int step;//第几次批上送 ，失败次数最多3次
                try {
                    step = Integer.valueOf(reserve5);
                } catch (Exception e) {
                    step = 1;
                }
                if (step >= 3) {
                    continue;
                }
                LogUtils.d("正在进行第" + requestCount + "笔批上送");
                handler.sendMessage(handler.obtainMessage(0x01,"正在进行第IC卡" + requestCount + "笔批上送"));
                requestCount++;
                Map<String, String> infoMap = TransactionPackageUtil.getBatchSendICInfo(context, transRecord);
                byte[] msgData = null;
                try {
                    msgData = nldPacketHandle.pack(aidlDeviceService, TransConstans.TRANS_CODE_BATCH_SEND_IC, infoMap, 1);
                } catch (Exception e1) {
                    LogUtils.e("IC卡组包过程发生异常"+ "]&&dataMap=[" + infoMap + "]");
                    e1.printStackTrace();
                    Cache.getInstance().setTransCode(NldException.ERR_DAT_PACK_E201);
                    Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DAT_PACK_E201));
                    handler.sendMessage(handler.obtainMessage(0x02));
                    break;
                }
                byte[] msg = nldPacketHandle.addMessageLen(msgData);
                LogUtils.d("IC卡发送加签的报文=[" + HexUtil.bcd2str(msg) + "]");
                try{
                    String resultStr = HttpConnetionHelper.httpClient.postHttpsUrl(uri, HexUtil.bcd2str(msg),handler);
                    if (null == resultStr) {  //网络访问失败
                        Cache.getInstance().setErrCode(NldException.ERR_NET_DEFAULT_E102);
                        Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_NET_DEFAULT_E102));
                        handler.sendMessage(handler.obtainMessage(0x02));
                        break;
                    }
                    LogUtils.d("IC卡接收前置返回的交易数据：[" + resultStr + "]");
                    byte[] pack = nldPacketHandle.subMessLen(HexUtil.hexStringToByte(resultStr));
                    LogUtils.d("IC卡返回的报文=[" + HexUtil.bcd2str(pack) + "]");
                    Map<String, String> resmap = nldPacketHandle.unPack(pack, "002311", 1);//解包
                    LogUtils.d("IC卡解包后resmap " + StringUtil.map2LineStr(resmap)+"/39域 rescode ="+ resmap.get("respcode"));
                    String respcode = resmap.get("respcode");
                    if (StringUtil.isEmpty(respcode)){
                        respcode = "00";
                    }
                    if ("00".equals(respcode)) {
                        mTransRecordDao.update(transRecord.getId(), "reserve5", SEND_SUCC);
                    } else {
                        mTransRecordDao.update(transRecord.getId(), "reserve5", step + "");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    LogUtils.d("IC卡批上送异常：" + e.getMessage());
                    String expcode = NldException.getExpCode(e, NldException.ERR_NET_DEFAULT_E102);
                    Cache.getInstance().setErrCode(expcode);
                    Cache.getInstance().setErrDesc(NldException.getMsg(expcode));
                    handler.sendMessage(handler.obtainMessage(0x02));
                    break;
                }
            }
            LogUtils.d("批上送IC卡交易完成");
        }
        handler.sendMessage(handler.obtainMessage(0x03));
    }

    @Override
    public byte[] getIsopack() {
        return new byte[0];
    }
}
