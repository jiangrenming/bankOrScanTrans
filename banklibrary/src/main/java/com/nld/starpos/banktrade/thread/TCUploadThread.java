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
 * TC上送线程
 */

public class TCUploadThread extends ComonThread {

    private AidlDeviceService mService;
    private String url;
    private NldPacketHandle nldPacketHandle;
    private TransRecordDao mTransRecordDao;
    //交易笔数
    private int number = 1;
    // 发送次数
    private int mSendTimes = 3;

    public TCUploadThread(Context context, Handler handler,AidlDeviceService service ,String uri) {
        super(context, handler);
        this.mService = service;
        this.url = uri;
        try {
            mTransRecordDao = new TransRecordDaoImpl();
            nldPacketHandle = new NldPacketHandle(context);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.i("NldPacketHandle is not initlized");
        }

    }


    @Override
    public void run() {
        List<TransRecord> mRecord = mTransRecordDao.getTransRecordsByStatuscode("TC");
        LogUtils.d("TC上送个数："+mRecord.size());
        if (!mRecord.isEmpty()) {
            final int maxTimes = mSendTimes;
            for (final TransRecord mR : mRecord) {
                int times = Integer.parseInt(mR.getReversetimes()) + 1;
                while (times <= maxTimes) {
                    handler.sendMessage(handler.obtainMessage(0x01,"正在第[" + number + "]笔\\n第[" + times+"]交易证书上送，请稍等..."));
                    LogUtils.d("正在第[" + number + "]笔\n第[" + times + "]次交易证书上送");
                    Map<String, String> infoMap = TransactionPackageUtil.getTCInfo(context,mR);

                    byte[] msgData = null;
                    try {
                        msgData = nldPacketHandle.pack(mService, TransConstans.TRANS_CODE_UPLOAD_TC, infoMap, 1);
                    } catch (Exception e1) {
                        LogUtils.e("TC交易组包过程发生异常:dataMap=[" + infoMap + "]", e1);
                        e1.printStackTrace();
                        Cache.getInstance().setTransCode(NldException.ERR_DAT_PACK_E201);
                        Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DAT_PACK_E201));
                        handler.sendMessage(handler.obtainMessage(0x02));
                        break;
                    }
                    byte[] msg = nldPacketHandle.addMessageLen(msgData);
                    LogUtils.d("TC交易发送加签的报文=[" + HexUtil.bcd2str(msg) + "]");
                    try{
                        String resultStr = HttpConnetionHelper.httpClient.postHttpsUrl(url, HexUtil.bcd2str(msg),handler);
                        if (null == resultStr) {  //网络访问失败
                            Cache.getInstance().setErrCode(NldException.ERR_NET_DEFAULT_E102);
                            Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_NET_DEFAULT_E102));
                            handler.sendMessage(handler.obtainMessage(0x02));
                            break;
                        }
                        LogUtils.d("TC交易接收前置返回的交易数据：[" + resultStr + "]");
                        byte[] pack = nldPacketHandle.subMessLen(HexUtil.hexStringToByte(resultStr));
                        LogUtils.d("TC交易返回的报文=[" + HexUtil.bcd2str(pack) + "]");
                        Map<String, String> resmap = nldPacketHandle.unPack(pack, "002311", 1);//解包
                        LogUtils.d("TC交易解包后resmap " + StringUtil.map2LineStr(resmap)+"/39域 rescode ="+ resmap.get("respcode"));
                        if ("00".equals(resmap.get("respcode"))) {
                            String resultMac = resmap.get("mesauthcode");
                            String calMac = nldPacketHandle.getMac(mService, pack);
                            LogUtils.d("TC交易计算mac:" + calMac + " 脱机交易消息返回" + resultMac);
                            if (calMac.equals(resultMac)) { //MAC验证
                                number ++; // 计数
                                mTransRecordDao.update(mR.getId(), "statuscode", "");
                                break;
                            } else {
                                if (times == maxTimes) {
                                    number ++; // 计数
                                    mTransRecordDao.update(mR.getId(), "statuscode", "");
                                    break;
                                } else {
                                    mTransRecordDao.update(mR.getId(), "reversetimes", String.valueOf((times))); //上送失败，更新重发次数
                                }
                            }
                        } else {  //TC上送失败
                            if (times >= maxTimes) {
                                number ++; // 计数
                                mTransRecordDao.update(mR.getId(), "statuscode", "");
                                break;
                            } else {
                                mTransRecordDao.update(mR.getId(), "reversetimes", String.valueOf((times))); //上送失败，更新重发次数
                            }
                        }
                        times++;
                    }catch (Exception e){
                        e.printStackTrace();
                        LogUtils.d("TC交易交易异常：" + e.getMessage());
                        String expcode = NldException.getExpCode(e, NldException.ERR_NET_DEFAULT_E102);
                        Cache.getInstance().setErrCode(expcode);
                        Cache.getInstance().setErrDesc(NldException.getMsg(expcode));
                        handler.sendMessage(handler.obtainMessage(0x02));
                        break;
                    }
                }
            }
            run();// 重新上送一次，保证所有交易均上送过
        } else { // 除结算外，其他所有联机交易之后上送脱机交易之后
            LogUtils.d("TC上送结束：" + number);
            handler.sendMessage(handler.obtainMessage(0x03));
            number = 1;
        }
    }

    @Override
    public byte[] getIsopack() {
        return new byte[0];
    }
}
