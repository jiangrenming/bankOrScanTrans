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
 * 刷卡批上送
 */

public class BatchEMVSendThread extends ComonThread {

    public static final String SEND_SUCC = "5";//批结对账不平 单笔批上送成功
    private TransRecordDao mTransRecordDao;
    private AidlDeviceService mDeviceService;
    private String uri;
    private NldPacketHandle nldPacketHandle;

    public BatchEMVSendThread(Context context, Handler handler, AidlDeviceService service,
                              String uri) {
        super(context, handler);
        this.mDeviceService = service;
        this.uri = uri;
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
        List<TransRecord> unBatchMagcardList = mTransRecordDao.getUnBatchMagcardList();
        LogUtils.d("刷卡批上送次数:" + unBatchMagcardList == null ? 0 : unBatchMagcardList.size());
        //上送次数
        int requestCount = 1;
        // 上送刷卡数据
        while (true) {
            if (unBatchMagcardList == null || unBatchMagcardList.size() == 0) {
                handler.sendMessage(handler.obtainMessage(0x03));
                break;
            }
            int flag = 1;//刷卡批上送 最多8条数据一传， flag 当前请求数量 ： 1、8条 ，2、大于0小于8 ，3、0条。
            List<TransRecord> tempTransRecordList = null;
            if (unBatchMagcardList.size() / 8 / requestCount > 0) {
                tempTransRecordList = unBatchMagcardList.subList(8 * (requestCount - 1), 8 * requestCount);
                flag = 1;
            } else {
                tempTransRecordList = unBatchMagcardList.subList(8 * (requestCount - 1), unBatchMagcardList.size());
                if (tempTransRecordList.size() == 0) {
                    handler.sendMessage(handler.obtainMessage(0x03));
                    break;
                } else {
                    flag = 2;
                }
            }
            LogUtils.d("正在进行第" + requestCount + "笔批上送");
            handler.sendMessage(handler.obtainMessage(0x01,"正在进行第" + requestCount + "笔批上送"));
            Map<String, String> unBacthMagcards = TransactionPackageUtil.getBatchSendEndInfo(context, tempTransRecordList,1);
            byte[] msgData = null;
            try {
                msgData = nldPacketHandle.pack(mDeviceService, TransConstans.TRANS_CODE_BATCH_SEND_END, unBacthMagcards, 1);
            } catch (Exception e1) {
                LogUtils.e("组包过程发生异常"+ "]&&dataMap=[" + unBacthMagcards + "]");
                e1.printStackTrace();
                Cache.getInstance().setTransCode(NldException.ERR_DAT_PACK_E201);
                Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DAT_PACK_E201));
                handler.sendMessage(handler.obtainMessage(0x02));
                break;
            }
            byte[] msg = nldPacketHandle.addMessageLen(msgData);
            LogUtils.d("发送加签的报文=[" + HexUtil.bcd2str(msg) + "]");
            try{
                String resultStr = HttpConnetionHelper.httpClient.postHttpsUrl(uri, HexUtil.bcd2str(msg),handler);
                if (null == resultStr) {  //网络访问失败
                    Cache.getInstance().setErrCode(NldException.ERR_NET_DEFAULT_E102);
                    Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_NET_DEFAULT_E102));
                    handler.sendMessage(handler.obtainMessage(0x02));
                    break;
                }
                LogUtils.d("接收前置返回的交易数据：[" + resultStr + "]");
                byte[] pack = nldPacketHandle.subMessLen(HexUtil.hexStringToByte(resultStr));
                LogUtils.d("返回的报文=[" + HexUtil.bcd2str(pack) + "]");
                Map<String, String> resmap = nldPacketHandle.unPack(pack, "002311", 1);//解包
                LogUtils.d("解包后resmap " + StringUtil.map2LineStr(resmap)+"/39域 rescode ="+ resmap.get("respcode"));
                String respcode = resmap.get("respcode");
                if (StringUtil.isEmpty(respcode)){
                    respcode = "00" ;
                }
                if ("00".equals(respcode)) {
                    updateReserve5ForSucc(unBatchMagcardList);
                } else {            //接到数据，但是交易处理不成功
                    updateReserve5ForFail(unBatchMagcardList);
                }
                requestCount++;
                if (flag == 2) {
                    LogUtils.d("批上送刷卡数据上送完毕");
                    handler.sendMessage(handler.obtainMessage(0x03));
                    break;
                }
            }catch (Exception e){
                e.printStackTrace();
                LogUtils.d("刷卡批上送异常：" + e.getMessage());
                //交易超时需要冲正。
                String expcode = NldException.getExpCode(e, NldException.ERR_NET_DEFAULT_E102);
                Cache.getInstance().setErrCode(expcode);
                Cache.getInstance().setErrDesc(NldException.getMsg(expcode));
                handler.sendMessage(handler.obtainMessage(0x02));
                break;
            }
        }
    }

    /**
     * 更新批上送成功标识
     * @param unBatchMagcardList
     */
    private void updateReserve5ForSucc(List<TransRecord> unBatchMagcardList) {
        for (TransRecord record : unBatchMagcardList) {
            mTransRecordDao.update(record.getId(), "reserve5", SEND_SUCC);
        }
    }

    /**
     * 更新批上送失败标识
     * @param unBatchMagcardList
     */
    private void updateReserve5ForFail(List<TransRecord> unBatchMagcardList) {
        for (TransRecord record : unBatchMagcardList) {
            String reserve5 = record.getReserve5();
            int step;//第几次批上送 ，失败次数最多3次
            try {
                step = Integer.valueOf(reserve5) + 1;
            } catch (Exception e) {
                step = 1;
            }
            mTransRecordDao.update(record.getId(), "reserve5", step + "");
        }
    }
    @Override
    public byte[] getIsopack() {
        return new byte[0];
    }

}
